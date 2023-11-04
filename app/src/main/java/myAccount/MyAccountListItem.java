package myAccount;


public class MyAccountListItem {
    private int iconResId; // Resource ID of the icon
    private String text;

    public MyAccountListItem(int iconResId, String text) {
        this.iconResId = iconResId;
        this.text = text;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getText() {
        return text;
    }
}
