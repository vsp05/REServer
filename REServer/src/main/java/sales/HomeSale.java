package sales;

// Simple class to provide test data in SalesDAO
@SuppressWarnings({"PMD.DataClass", "PMD.ExcessiveParameterList"})

public class HomeSale {
  public int property_id;
  public String download_date;
  public String council_name;
  public int purchase_price;
  public String address;
  public int post_code;
  public String property_type;
  public String strata_lot_number;
  public String property_name;
  public int area;
  public String area_type;
  public String contract_data;
  public String settlement_date;
  public String zoning;
  public String nature_of_property;
  public String primary_purpose;
  public String legal_description;

  public HomeSale(int property_id, String download_date, String council_name, int purchase_price, String address, int post_code,
                  String property_type, String strata_lot_number, String property_name, int area, String area_type, String contract_data,
                  String settlement_date, String zoning, String nature_of_property, String primary_purpose, String legal_description) {
    this.property_id = property_id;
    this.download_date = download_date;
    this.council_name = council_name;
    this.purchase_price = purchase_price;
    this.address = address;
    this.post_code = post_code;
    this.property_type = property_type;
    this.strata_lot_number = strata_lot_number;
    this.property_name = property_name;
    this.area = area;
    this.area_type = area_type;
    this.contract_data = contract_data;
    this.settlement_date = settlement_date;
    this.zoning = zoning;
    this.nature_of_property = nature_of_property;
    this.primary_purpose = primary_purpose;
    this.legal_description = legal_description;
  }

  // needed for JSON conversion
  public HomeSale() {
  }
}
