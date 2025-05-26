package dashboard;

import javafx.beans.property.*;

public class SalesOfftake {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty productName = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final StringProperty date = new SimpleStringProperty();

    public SalesOfftake() {
    }

    public SalesOfftake(String id, String productName, int quantity, double price, String date) {
        setId(id);
        setProductName(productName);
        setQuantity(quantity);
        setPrice(price);
        setDate(date);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }
    public void setId(String id) { this.id.set(id); }

    public String getProductName() { return productName.get(); }
    public StringProperty productNameProperty() { return productName; }
    public void setProductName(String name) { this.productName.set(name); }

    public int getQuantity() { return quantity.get(); }
    public IntegerProperty quantityProperty() { return quantity; }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }

    public double getPrice() { return price.get(); }
    public DoubleProperty priceProperty() { return price; }
    public void setPrice(double price) { this.price.set(price); }

    public String getDate() { return date.get(); }
    public StringProperty dateProperty() { return date; }
    public void setDate(String date) { this.date.set(date); }
}