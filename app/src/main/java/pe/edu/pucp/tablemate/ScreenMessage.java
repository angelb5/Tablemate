package pe.edu.pucp.tablemate;

import java.io.Serializable;

public class ScreenMessage implements Serializable {
    private int topDrawable;
    private int bottomImage;
    private String titleText;
    private String messageText;
    private String btnText;
    private Class nextActivity;

    public ScreenMessage() {
    }

    public ScreenMessage(int topDrawable, int bottomImage, String titleText, String messageText, String btnText, Class nextActivity) {
        this.topDrawable = topDrawable;
        this.bottomImage = bottomImage;
        this.titleText = titleText;
        this.messageText = messageText;
        this.btnText = btnText;
        this.nextActivity = nextActivity;
    }

    public int getTopDrawable() {
        return topDrawable;
    }

    public void setTopDrawable(int topDrawable) {
        this.topDrawable = topDrawable;
    }

    public int getBottomImage() {
        return bottomImage;
    }

    public void setBottomImage(int bottomImage) {
        this.bottomImage = bottomImage;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getBtnText() {
        return btnText;
    }

    public void setBtnText(String btnText) {
        this.btnText = btnText;
    }

    public Class getNextActivity() {
        return nextActivity;
    }

    public void setNextActivity(Class nextActivity) {
        this.nextActivity = nextActivity;
    }
}
