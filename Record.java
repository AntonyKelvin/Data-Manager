public class Record {
    Integer ID;
    String clientName;
    String phoneNumber;

    int MAX_ID = 4 , MAX_CLIENTNAME = 16 , MAX_PHONENUMBER = 12;

    public Record(Integer ID, String clientName,String phoneNumber)
    {
        this.ID = ID;
        this.clientName = clientName;
        this.phoneNumber = phoneNumber;
    }

    public boolean isValidRecord()
    {
        //Need to figure how much the Integer occupies(2^-32 to 2^32)
        if( (clientName.length() <= MAX_CLIENTNAME)  && (phoneNumber.length() <= MAX_PHONENUMBER))
        {
            return true;
        }
        return false;
    }


}
