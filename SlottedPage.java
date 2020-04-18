import java.util.ArrayList;
import java.util.HashMap;

class RecordMeta
{
    Integer numberOfBytes;
    Integer isFree;
    Record record;

    public RecordMeta(Integer numberOfBytes, Integer isFree, Record record) {
        this.numberOfBytes = numberOfBytes;
        this.isFree = isFree;
        this.record = record;
    }

}

public class SlottedPage {
    String table_name;
    Integer page_no;

    //contents[0] will have number of bytes
    //contents[1] will have free or not
    //contents[2] will have the tuple(space separated)
    ArrayList<RecordMeta>contents = new ArrayList<RecordMeta>();
    HashMap<Integer,Integer>id_map = new HashMap<Integer,Integer>();
    HashMap<String,Integer>area_map = new HashMap<String,Integer>();
    //Record[] contents;

    int cur_size = 0;
    int total_size = 60;

    public SlottedPage(String table_name,Integer page_no)
    {
        this.table_name  = table_name;
        this.page_no = page_no;
        //this.contents = contents;
    }


    public void print_slotted_page_contents()
    {
        System.out.println("Slotted page for table " + table_name + page_no + " starts");
        for(RecordMeta temp:contents)
        {
            System.out.println(temp.numberOfBytes+" "+temp.isFree+" "+temp.record.ID+" "+temp.record.phoneNumber+" "+temp.record.clientName);
        }
        System.out.println("Slotted page for table " + table_name + page_no + " ends");
        System.out.println();
    }

    //Add records to slotted page
    //content bytes/free/tuple# format
    public boolean is_add_record(String content)
    {
        //Check if by adding the record are we exceeding the tot_size
        //if not add and return true else return false
        if( (cur_size + content.length()) > total_size)
            return false;
        else
        {
            return true;
        }
    }

    public void add_record_to_slottedpage(String content)
    {
        content = content.replaceAll("\\s+", " ");
        String[] record_meta = content.trim().split("/");
        //System.out.println("Record meta 2 " + record_meta[2]);
        String[] record = record_meta[2].trim().split(" ");
        //System.out.println(record[0]+" "+record[1]+" "+record[2]);
        RecordMeta new_record = new RecordMeta(Integer.valueOf(record_meta[0]),
                                                Integer.valueOf(record_meta[1]),
                                    new Record(Integer.valueOf(record[0]),record[1],record[2]));
        //Update the total_size
        cur_size += record_meta[0].length()+1
                +record_meta[1].length()+1
                +record_meta[2].length()+1;

        //Add to contents
        contents.add(new_record);

        //Update ID hashmap
        id_map.put(Integer.valueOf(record[0]),1);
        //111-111-1111
        area_map.put(record[2].split("-")[0],1);
    }

    //Delete the records in the slotted page
    public void delete_record(SlottedPage cur_page)
    {
        //Make its free as 0 and update the update the meta data file for holes.
    }
}
