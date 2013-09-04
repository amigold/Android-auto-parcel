import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parcelator {

    private static final String FIELD_PATTERN = "(public|private|protected) (\\w*[<\\w>]{0,}) (\\w+).*";
    // private static String sourceCode;
    private static final String parcelTemplate = "public classname(Parcel in) {"
                    + "    }"
                    + ""
                    + "    @Override"
                    + "    public int describeContents() {"
                    + "        return 0;\n"
                    + "    }\n"
                    + "    @Override\n"
                    + "    public void writeToParcel(Parcel dest, int flags) {"
                    + "    }"
                    + ""
                    + "    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {\n"
                    + "        public classname createFromParcel(Parcel in) {\n"
                    + "            return new classname(in);\n"
                    + "        }\n"
                    + "        public classname[] newArray(int size) {\n"
                    + "            return new classname[size];\n"
                    + "        }\n" + "    };\n";
    private static ArrayList<String> membersList;
    private static ArrayList<String> booleansList;
    private static String sourceCode;
    private static String sourceFile;

    /**
     * @param args
     */
    public static void main(String[] args) {

        sourceFile = args[0];

        sourceCode = null;
        try {
            sourceCode = FileTools.readFile(sourceFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        addParcel();

         try {
         FileTools.writeFile(sourceFile, sourceCode);
         } catch (IOException e) {
         e.printStackTrace();
         }
    }

    private static void addParcel() {
        // Scanner scan = new Scanner(sourceCode);

        addImportsAndDeclaration();

        membersList = createMemberList();

        addParcelTemplate();

//        System.out.println(sourceCode);
    }

    private static void addParcelTemplate() {
        Pattern impPattern = Pattern.compile("public class (\\w{0,})");
        Matcher match = impPattern.matcher(sourceCode);

        match.find();
        String className = match.group(1);

        String updatedTemplate = parcelTemplate.replace("classname", className);

        Pattern writePat = Pattern.compile("public void writeToParcel[^{]*\\{");
        match = writePat.matcher(updatedTemplate);

        match.find();
        StringBuilder builder = new StringBuilder(updatedTemplate);
        builder.insert(match.end(), generateMemberWrites());

        Pattern readPat = Pattern.compile("public \\w*\\(Parcel in\\) \\{");
        match = readPat.matcher(updatedTemplate);
        match.find();
        builder.insert(match.end(), generateMemberReads());

        // quick and dirty cause im tired
        StringBuilder builderSource = new StringBuilder(sourceCode);
        builderSource.deleteCharAt(builderSource.lastIndexOf("}"))
                        .append(builder).append("}");

        sourceCode = builderSource.toString();
    }

    private static String generateMemberReads() {
        Pattern memeberPat = Pattern.compile(FIELD_PATTERN);

        StringBuilder builder = new StringBuilder();
        for (String member : membersList) {
            Matcher match = memeberPat.matcher(member);
            match.find();
            String type = match.group(2);
            String name = match.group(3);

            String line;

            if (type.contains("int")) {
                if (type.contains("[]"))
                    line = "in.readIntArray(" + name + ");";
                else
                    line = "this." + name + " = in.readInt();";
            } else if (type.contains("List")) {
                line = "in.readTypedList(" + name + ", " + type + ".CREATOR);";
            } else if (type.contains("long")) {
                if (type.contains("[]"))
                    line = "in.readLongArray(" + name + ");";
                else
                    line = "this." + name + " = in.readLong();";
            } else if (type.contains("double")) {
                if (type.contains("[]"))
                    line = "in.readDoubleArray(" + name + ");";
                else
                    line = "this." + name + " = in.readDouble();";
            } else if (type.contains("String")) {
                if (type.contains("[]"))
                    line = "in.readStringArray(" + name + ");";
                else
                    line = "this." + name + " = in.readString();";
            } else if (type.contains("Date")) {
                line = "this." + name + "= new Date(in.readLong());";
            } else {
                line = "this." + name + "= in.readParcelable(" + type
                                + ".class.getClassLoader());";
            }

            builder.append(line).append("\n");
        }

        // now handle booleans
        if (booleansList.size() > 0) {
            builder.append("boolean[] temp = new boolean["
                            + booleansList.size() + "];\n");
            builder.append("in.readBooleanArray(temp);\n");

            for (int i = 0; i < booleansList.size(); i++) {
                Matcher match = memeberPat.matcher(booleansList.get(i));
                match.find();
                String name = match.group(2);

                builder.append(name + "= temp[" + i + "];\n");
            }
        }

        return builder.toString();
    }

    private static String generateMemberWrites() {
        Pattern memeberPat = Pattern.compile(FIELD_PATTERN);

        StringBuilder builder = new StringBuilder();
        for (String member : membersList) {
            Matcher match = memeberPat.matcher(member);
            match.find();
            String type = match.group(2);
            String name = match.group(3);

            String line;

            if (type.contains("int")) {
                if (type.contains("[]"))
                    line = "dest.writeIntArray(this." + name + ");";
                else
                    line = "dest.writeInt(this." + name + ");";
            } else if (type.contains("List")) {
                line = "dest.writeTypedList(this." + name + ");";
            } else if (type.contains("long")) {
                if (type.contains("[]"))
                    line = "dest.writeLongArray(this." + name + ");";
                else
                    line = "dest.writeLong(this." + name + ");";
            } else if (type.contains("double")) {
                if (type.contains("[]"))
                    line = "dest.writeDoubleArray(this." + name + ");";
                else
                    line = "dest.writeDouble(this." + name + ");";
            } else if (type.contains("String")) {
                if (type.contains("[]"))
                    line = "dest.writeStringArray(this." + name + ");";
                else
                    line = "dest.writeString(this." + name + ");";
            } else if (type.contains("Date")) {
                line = "if(" + name + " == null)\n " + name
                                + " = new Date();\n";
                line += "dest.writeLong(" + name + ".getTime());";
            } else {
                line = "dest.writeParcelable(" + name + ", flags);";
            }

            builder.append(line).append("\n");
        }

        // now handle booleans
        if (booleansList.size() > 0) {
            String boolInit = "dest.writeBooleanArray(new boolean[] {";
            String boolEnd = "});";

            builder.append(boolInit);

            for (String member : booleansList) {
                Matcher match = memeberPat.matcher(member);
                match.find();
                String name = match.group(2);

                builder.append(name + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(boolEnd);
        }

        return builder.toString();
    }

    private static ArrayList<String> createMemberList() {
        Pattern memeberPat = Pattern.compile(FIELD_PATTERN);
        Matcher match = memeberPat.matcher(sourceCode);

        membersList = new ArrayList<String>();
        booleansList = new ArrayList<String>();

        while (match.find()) {
            
            if(match.group(0).contains("{"))
                continue;
            
            if (match.group(1).contains("boolean")) {
                booleansList.add(match.group());
            } else
                membersList.add(match.group());
        }

        return membersList;
    }

    private static void addImportsAndDeclaration() {
//        sourceCode = "import android.os.Parcel;" + "\n"
//                        + "import android.os.Parcelable;" + "\n" + sourceCode;

        // lets find the class def
        Pattern impPattern = Pattern.compile("public class [^\\{]*");
        Matcher match = impPattern.matcher(sourceCode);

        if (match.find()) {
            String found = match.group();
            String newFound;
            // System.out.println(found);

            if (found.contains("implements")) {
                newFound = found.concat(", Parcelable");
            } else {
                newFound = found.concat("implements Parcelable");
            }

            sourceCode = sourceCode.replace(found, newFound);
            // System.out.println(sourceCode);
        }
    }
}
