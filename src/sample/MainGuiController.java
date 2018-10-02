package sample;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class MainGuiController implements Initializable {

    @FXML private TextField poo;

    private NamesCollection namesCollection = new NamesCollection();
    private PopupAlert alert = new PopupAlert();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.namesCollection.solveAllNames();
        }catch (IOException e){
            this.alert.unkownError();
        }

        Set<String> autoCompletions = new HashSet<>(this.namesCollection.getAllNamesFirstCap());
        SuggestionProvider<String> provider = SuggestionProvider.create(autoCompletions);
        new AutoCompletionTextFieldBinding<>(this.poo, provider);
        this.poo.addEventFilter(KeyEvent.KEY_RELEASED, event-> {
            if(event.getCode()==KeyCode.SPACE || event.getCode()==KeyCode.MINUS || event.getCode()==KeyCode.UNDERSCORE) {
                Set<String> filteredAutoCompletions = new HashSet<>(this.namesCollection.addPrefixToAllNamesFirstCap(this.poo.getText()));
                provider.addPossibleSuggestions(filteredAutoCompletions);
            }
        });
    }
}
