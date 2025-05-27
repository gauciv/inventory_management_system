package dashboard;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Inventory_management_bin {
    // Existing properties for item data 
    private final SimpleIntegerProperty item_code;
    private final SimpleStringProperty item_des;
    private final SimpleIntegerProperty volume;
    private final SimpleStringProperty category;
    private final SimpleIntegerProperty sot;
    private final SimpleIntegerProperty soh;
    
    // New property for checkbox selection
    private final SimpleBooleanProperty selected;

    public Inventory_management_bin(Integer item_code, String item_des, Integer volume, String category, Integer sot, Integer soh) {
        this.item_code = new SimpleIntegerProperty(item_code);
        this.item_des = new SimpleStringProperty(item_des);
        this.volume = new SimpleIntegerProperty(volume);
        this.category = new SimpleStringProperty(category);
        this.soh = new SimpleIntegerProperty(soh);
        this.sot = new SimpleIntegerProperty(sot);
        // Initialize checkbox as unselected
        this.selected = new SimpleBooleanProperty(false);
    }

    // Existing getters
    public Integer getItem_code() {
        return item_code.get();
    }    public String getItem_des() {
        return item_des.get();
    }
    
    public String getFormattedItemDesc() {
        String desc = getItem_des();
        if (desc != null) {
            return desc.trim(); // Remove any extra whitespace
        }
        return "";
    }

    public Integer getVolume() {
        return volume.get();
    }

    public String getCategory() {
        return category.get();
    }

    public Integer getSot() {
        return sot.get();
    }

    public Integer getSoh() {
        return soh.get();
    }

    // New methods for checkbox selection
    public Boolean getSelected() {
        return selected.get();
    }

    public void setSelected(Boolean value) {
        selected.set(value);
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }
}
