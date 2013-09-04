public class User {

    private int bla;
    protected int lol;
    public int userId = -1;
    public int facebookId = -1;
    public String facebookToken;
    public String apnsToken;

    public enum UserType {
        AppUser(4), UnregisteredUser(5);

        public int type;

        UserType(int type) {
            this.type = type;
        }
    }

    public User() {
    }

    public User(UserType userType, int facebookId, String facebookToken,
                    String apnsToken) {
        super();
        this.facebookId = facebookId;
        this.facebookToken = facebookToken;
        this.apnsToken = apnsToken;
    }

}
