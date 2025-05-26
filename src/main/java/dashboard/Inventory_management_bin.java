package dashboard;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Inventory_management_bin {
    private final SimpleIntegerProperty item_code;
    private final SimpleStringProperty item_des;
    private final SimpleIntegerProperty volume;
    private final SimpleStringProperty category;
    private final SimpleIntegerProperty sot;
    private final SimpleIntegerProperty soh;


    public Inventory_management_bin(Integer item_code, String item_des, Integer volume, String category, Integer sot, Integer soh) {
        this.item_code = new SimpleIntegerProperty(item_code);
        this.item_des = new SimpleStringProperty(item_des);
        this.volume = new SimpleIntegerProperty(volume);
        this.category = new SimpleStringProperty(category);
        this.soh = new SimpleIntegerProperty(soh);
        this.sot = new SimpleIntegerProperty(sot);
    }

    // Getters need to match exactly what's in the PropertyValueFactory
    public Integer getItem_code() {
        return item_code.get();
    }

    public String getItem_des() {
        return item_des.get();
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

}
