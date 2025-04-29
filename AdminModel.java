package nfc;

public class AdminModel {
    private static String name;
    private static String profilePicture;

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        AdminModel.name = name;
    }

    public static String getProfilePicture() {
        return profilePicture;
    }

    public static void setProfilePicture(String profilePicture) {
        AdminModel.profilePicture = profilePicture;
    }
}
