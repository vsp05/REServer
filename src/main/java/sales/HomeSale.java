package sales;

@SuppressWarnings("PMD.TooManyFields")

// Simple class to provide test data in SalesDAO
public class HomeSale {
    
    public int propertyId;
    public String downloadDate;
    public String councilName;
    public int purchasePrice;
    public String address;
    public int postCode;
    public String propertyType;
    public String strataLotNumber;
    public String propertyName;
    public int area;
    public String areaType;
    public String contractData;
    public String settlementDate;
    public String zoning;
    public String natureOfProperty;
    public String primaryPurpose;
    public String legalDescription;


    public HomeSale(final int propertyId, final String downloadDate, final String councilName, final int purchasePrice, final String address, final int postCode,
    final String propertyType, final String strataLotNumber, final String propertyName, final int area, final String areaType, final String contractData, 
    final String settlementDate, final String zoning, final String natureOfProperty, final String primaryPurpose, final String legalDescription) {
       this.propertyId = propertyId;
       this.downloadDate = downloadDate;
       this.councilName = councilName;       
       this.purchasePrice = purchasePrice;
       this.address = address;
       this.postCode = postCode;
       this.propertyType = propertyType;
       this.strataLotNumber = strataLotNumber;
       this.propertyName = propertyName;
       this.area = area;
       this.areaType = areaType;
       this.contractData = contractData;
       this.settlementDate = settlementDate;
       this.zoning = zoning;
       this.natureOfProperty = natureOfProperty;
       this.primaryPurpose = primaryPurpose;
       this.legalDescription = legalDescription;
   }

    // needed for JSON conversion
    public HomeSale() {}


}
