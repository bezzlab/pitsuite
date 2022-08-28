package graphics;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.controlsfx.control.Notifications;

public class CopyableText extends Text{

    public CopyableText(String text) {
        super(text);
        setCopyText(text);

    }

    public void setCopyText(String text){
        if(getOnMouseClicked()!=null)
            removeEventHandler(MouseEvent.MOUSE_CLICKED, getOnMouseClicked());
        setOnMouseClicked(event -> {
            if(event.getButton()== MouseButton.SECONDARY){
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                clipboard.setContent(content);
                Notifications.create()
                        .title("Copy")
                        .text("Copied "+(text.length()<50?text:(text.substring(0,50)+"..."))+" to clipboard")
                        .show();
            }
        });

    }
}
