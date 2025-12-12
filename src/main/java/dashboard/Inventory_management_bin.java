package dashboard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Inventory_management_bin {
    private int item_code;
    private String item_des;
    private int volume;
    private String category;
    private int sot;
    private int soh;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Inventory_management_bin(int item_code, String item_des, int volume, String category, int sot, int soh) {
        this.item_code = item_code;
        this.item_des = item_des;
        this.volume = volume;
        this.category = category;
        this.sot = sot;
        this.soh = soh;
    }

    public int getItem_code() { return item_code; }
    public void setItem_code(int item_code) { this.item_code = item_code; }

    public String getItem_des() { return item_des; }
    public void setItem_des(String item_des) { this.item_des = item_des; }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSot() { return sot; }
    public void setSot(int sot) { this.sot = sot; }

    public int getSoh() { return soh; }
    public void setSoh(int soh) { this.soh = soh; }

    public BooleanProperty selectedProperty() { return selected; }
    public boolean getSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
    
    public String getFormattedItemDesc() {
        return item_des + " (" + volume + "ml)";
    }
}