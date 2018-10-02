package sample.customcomponent;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynamicAutocompleteTextBox extends TextField {

    private List<String> initalAutocompleteListItems;
    private List<String> currentAutocompleteListItems;
    private SuggestionProvider<String> provider;

    public DynamicAutocompleteTextBox(){ }

    public void setAutocompleteList(List<String> items) {
        this.initalAutocompleteListItems = items;
        this.currentAutocompleteListItems = items;
        Set<String> autoCompletions = new HashSet<>(this.initalAutocompleteListItems);
        provider = SuggestionProvider.create(autoCompletions);
        new AutoCompletionTextFieldBinding<>(this, provider);

        this.addEventFilter(KeyEvent.KEY_RELEASED, event-> {
            if(event.getCode()== KeyCode.SPACE || event.getCode()==KeyCode.MINUS || event.getCode()==KeyCode.UNDERSCORE) {
                Set<String> filteredAutoCompletions = new HashSet<>(updateAutocompleteListItems());
                provider.addPossibleSuggestions(filteredAutoCompletions);
            }
        });
    }

    public void resetAutocompleteTextBox() {
        Set<String> filteredAutoCompletions = new HashSet<>(initalAutocompleteListItems);
        provider.clearSuggestions();
        provider.addPossibleSuggestions(filteredAutoCompletions);
    }

    private List<String> updateAutocompleteListItems() {
        List<String> added = new ArrayList<>();
        for(String i: this.currentAutocompleteListItems){
            added.add(this.getText() + i);
        }
        return added;
    }

}
