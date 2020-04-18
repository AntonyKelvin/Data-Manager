import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class TablePage
{
    String table_name;
    Integer page_number;

    public TablePage(String table_name,Integer page_number)
    {
        this.table_name = table_name;
        this.page_number = page_number;
    }
}

//NOTE: All comparisons on the slotted page should be based on the table name and page number.

public class Memory {
    //Memory will have an arraylist of slotted pages
    ArrayList<SlottedPage> slotted_pages = new ArrayList<SlottedPage>();
    public int max_size = 10;
    public int cur_size = 0;
    //HashMap<TablePage,Boolean>is_available_page = new HashMap<TablePage,Boolean>();

    public void printIDinBuffer(String table_name,Integer ID,int index) throws IOException
    {
        SlottedPage temp = slotted_pages.get(index);
        for(RecordMeta tuple:temp.contents)
        {
            if(tuple.record.ID == ID && tuple.isFree == 1)
            {
                System.out.println(tuple.numberOfBytes);
                System.out.println(tuple.isFree);
                System.out.println(tuple.record.ID+" "+tuple.record.clientName+" "+tuple.record.phoneNumber);
                Main.log_writer.write("Read: "+table_name+","+tuple.record.ID+","+tuple.record.clientName+","+tuple.record.phoneNumber);
                Main.log_writer.write("\n");
                break;
            }
        }
    }

    public void areaSearchInDisk(String table_name,String area,Integer lastPageNo,ArrayList<RecordMeta>query_results,HashMap<String,Integer>visited_pages) throws IOException
    {
        System.out.println("Contents in the hashmap");
        Iterator hmIterator = visited_pages.entrySet().iterator();
        while(hmIterator.hasNext())
        {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            System.out.println(mapElement.getKey()+" "+mapElement.getValue());
        }
        System.out.println("Contents in the hashmap");
        for(int i=1;i<=lastPageNo;i++)
        {

            if(!visited_pages.containsKey(String.valueOf(i)))
            {
                System.out.println("_________________________________");
                System.out.println("The table name is " + table_name);
                System.out.println("The page number is " + i);
                for(SlottedPage temp: slotted_pages)
                {
                    if(temp.table_name.equals(table_name))
                    {
                        System.out.println(temp.page_no);
                    }
                }
                System.out.println("__________________________________");
                //Iterate through the page find if there is a match.
                //If there is a match then add that page to the buffer
                //and add results to query_results
                File search_table_disk = new File("Database//"+table_name+"//"+table_name+i+".txt");
                BufferedReader id_reader = new BufferedReader(new FileReader(search_table_disk));

                String line = "";
                boolean isContains = false;
                while( (line = id_reader.readLine()) != null)
                {
                    System.out.println("_________________________________");
                    System.out.println("The table name is " + table_name);
                    System.out.println("The page number is " + i);
                    System.out.println("The line is " + line);
                    System.out.println("__________________________________");
                    String[] tuples = line.split("#");
                    for(String temp: tuples)
                    {
                        //record_meta[2] contains the tuple
                        String[] record_meta = temp.split("/");

                        //record[0] contains the ID
                        //record[2] contains the phone 412-111-1111
                        String[] record = record_meta[2].split(" ");

                        String tuple_area = record[2].split("-")[0];
                        System.out.println(tuple_area+" "+area);
                        if(tuple_area.equals(area) && Integer.valueOf(record_meta[1]) == 1)
                        {
                            loadPageToDisk(table_name,i);
                            //Add the query results
                            searchAreaInBuffer(table_name,area,query_results);
                            id_reader.close();
                            isContains = true;
                            break;
                        }

                    }
                    if (isContains)
                    {
                        break;
                    }
                }
            }
        }
    }

    //IdSearchInDisk(String table_name)
    public boolean IdSearchInDisk(String table_name,Integer ID,Integer lastPage_no) throws IOException
    {
        //Iterate through all the pages in the disk and find the match for the ID
        int index = -1;

        System.out.println("Last page is " + lastPage_no);

        for(int i=1;i<=lastPage_no;i++)
        {
            //System.out.println("_____________TABLE " + table_name + " PAGE " + i+"___________");
            File search_table_disk = new File("Database//"+table_name+"//"+table_name+i+".txt");
            BufferedReader id_reader = new BufferedReader(new FileReader(search_table_disk));

            String line = "";
            boolean isClose = false;
            while( (line = id_reader.readLine()) != null)
            {
                String[] tuples = line.split("#");
                for(String temp: tuples)
                {
                    //record_meta[2] contains the tuple
                    String[] record_meta = temp.split("/");
                    //record[0] contains the ID
                    String[] record = record_meta[2].split(" ");

                    System.out.println(record[0]+" "+ID);
                    System.out.println(record_meta[1]);
                    if(Integer.valueOf(record[0]) == ID && record_meta[1].equals("1"))
                    {
                        index = i;
                        //id_reader.close();
                        isClose = true;
                        break;
                    }

                }
                if(isClose)
                {
                    id_reader.close();
                    break;
                }
            }
            //System.out.println("_____________________________________________________");

        }

        if(index != -1)
        {
            //Bring that page to the buffer
            loadPageToDisk(table_name,index);
            return true;
        }
        else
        {
            //System.out.println("Sorry the record doresn't exists!");
            return false;
        }
    }


    //Brining in the slotted page to the memory buffer

    //This method will return if the current page is in the buffer or not(will return the position of that page
    //in arraylist)

    public void delete_from_buffer(String table_name)
    {
        boolean[] is_delete = new boolean[max_size];
        int new_size = 0;

        for(int i=0;i<max_size;i++)
        {
            is_delete[i] = false;
        }

        int index = 0;

        for(SlottedPage temp: slotted_pages)
        {
            if(temp.table_name.equals(table_name))
            {
                is_delete[index] = true;
            }
            else
            {
                new_size++;
            }
            index++;
        }

        ArrayList<SlottedPage>temp = new ArrayList<SlottedPage>();
        for(int i=0;i<cur_size;i++)
        {
            if(!is_delete[i])
            {
                temp.add(slotted_pages.get(i));
            }
        }

        //Update the slotted page arraylist and update cur size
        slotted_pages = (ArrayList<SlottedPage>) temp.clone();
        temp.clear();

        cur_size = new_size;


    }

    public int isIDinBuffer(String table_name,Integer ID)
    {
        int index = 0;
        for(SlottedPage temp:slotted_pages)
        {
            if( (temp.table_name.equals(table_name)) && (temp.id_map.containsKey(ID)))
            {
                System.out.println("Match found for deletion!");
                return index;
            }
            index++;
        }
        return -1;
    }


    public Integer isInBuffer(String table_name,Integer page_no)
    {
        int index = 0;
        for(SlottedPage temp:slotted_pages)
        {
            if(temp.table_name.equals(table_name) && temp.page_no == page_no)
            {
                return index;
            }
            index++;
        }
        return -1;
    }

    //When adding the page to the slotted page check if the page is in the buffer
    //if yes make it most recent accessed or else add it to the page
    public SlottedPage getPage(int i)
    {
        return slotted_pages.get(i);
    }

    //Update is done only when the new record size is lesser or equal to the original size
    public void update_record_in_buffer(int index,Integer ID,RecordMeta update_record) throws IOException
    {
        SlottedPage temp = slotted_pages.get(index);
        //System.out.println("UPDATE DONE IN BUFFER ");
        //System.out.println(temp.table_name+" "+ID);
        //System.out.println("UPDATE DONE IN BUFFER");
        for(RecordMeta tuple:temp.contents)
        {

            if(tuple.record.ID == ID && tuple.isFree == 1)
            {
                //Update the record
                //System.out.println(tuple.numberOfBytes+" "+update_record.numberOfBytes);
                if(tuple.numberOfBytes >= update_record.numberOfBytes)
                {
                    //Then you can update
                    tuple.numberOfBytes = update_record.numberOfBytes;
                    tuple.record.ID = update_record.record.ID;
                    tuple.record.clientName = update_record.record.clientName;
                    tuple.record.phoneNumber = update_record.record.phoneNumber;
                }
                else
                {
                    System.out.println("Sorry it exceeds the size!");
                }
            }
        }
    }

    public void delete_record_in_buffer(int index,Integer ID) throws IOException
    {
        SlottedPage temp = slotted_pages.get(index);

        System.out.println("______Slotted page going to delete______");
        for(RecordMeta record : temp.contents)
        {
            System.out.println(record.record.ID+" "+record.record.clientName);
        }

        for(RecordMeta tuple:temp.contents)
        {
            if(tuple.record.ID == ID)
            {

                System.out.println("Deleted name "+ tuple.record.clientName);
                tuple.isFree = 0;
                //Update the hashmap
                temp.id_map.remove(ID);

                //Write to meta file regarding the hole
                int page_no = temp.page_no;
                String table_name = temp.table_name;
                int number_of_bytes = tuple.numberOfBytes;

                File meta_hole = new File("Database//"+table_name+"//meta"+table_name+".txt");
                if(meta_hole.createNewFile())
                {
                    System.out.println("File created successfully");

                }
                BufferedWriter meta_write = new BufferedWriter(new FileWriter(meta_hole,true));
                meta_write.append(table_name+" "+page_no+" "+number_of_bytes);
                meta_write.close();

                add_to_buffer(temp);
                break;
            }
        }
    }

    public void add_to_buffer(SlottedPage cur_page) throws IOException
    {
        int index = isInBuffer(cur_page.table_name,cur_page.page_no);

        if(index == -1)
        {
            if( (cur_size + 1) > max_size)
            {
                System.out.println("Overflow in main memory " + cur_size);

                evict();
            }
            else
            {
                cur_size++;
            }
            Main.log_writer.write("SWAP IN T-"+cur_page.table_name+" P-"+cur_page.page_no);
            Main.log_writer.write("\n");
            slotted_pages.add(cur_page);
            //System.out.println("Size of the buffer " + slotted_pages.size());

        }

        else
        {
            //Making it to most recently accessed
            slotted_pages.remove(index);
            slotted_pages.add(cur_page);
        }
        //is_available_page.put(new TablePage(cur_page.table_name,cur_page.page_no),true);
    }

    public void searchAreaInBuffer(String table_name,String area,ArrayList<RecordMeta>query_results) throws IOException
    {
        //It should be the last page only but still we are iterating
        //If it works fine
        for(SlottedPage page : slotted_pages)
        {
            if(!page.table_name.equals(table_name)) continue;

            boolean isContains = false;
            for(RecordMeta tuple: page.contents)
            {
                if(tuple.record.phoneNumber.split("-")[0].equals(area) && tuple.isFree == 1)
                {
                    query_results.add(tuple);
                    isContains = true;
                }
            }

            /*
            if(isContains)
            {
                add_to_buffer(page);

            }

            */

        }
    }


    public HashMap<String,Integer> searhAreaInBuffer(String table_name,String area,ArrayList<RecordMeta>query_results) throws IOException
    {

        HashMap<String,Integer>temp = new HashMap<String,Integer>();

        ArrayList<Integer>all_pages_no = new ArrayList<Integer>();
        ArrayList<SlottedPage>all_pages = new ArrayList<SlottedPage>();

        for(SlottedPage page : slotted_pages)
        {
            if(!page.table_name.equals(table_name)) continue;

            boolean isContains = false;
            for(RecordMeta tuple: page.contents)
            {
                System.out.println(tuple.record.phoneNumber.split("-")[0]+" "+area);
                System.out.println(tuple.record.clientName+" "+tuple.isFree);
                if(tuple.record.phoneNumber.split("-")[0].equals(area) && tuple.isFree == 1)
                {
                    System.out.println("Adding to query result");
                    query_results.add(tuple);
                    isContains = true;
                }
            }

            //if(isContains)
            //{
                //System.out.println("PAGE NUMBER " + page.page_no + "has a tuple with area code");
                all_pages_no.add(page.page_no);
                all_pages.add(page);
                //temp.put(String.valueOf(page.page_no),1);
                //Make it recent
                //add_to_buffer(page);

            //}

        }

        //System.out.println("All pages have a size " + all_pages.size());
        for(int i=0;i<all_pages.size();i++)
        {

            temp.put(String.valueOf(all_pages_no.get(i)),1);
            add_to_buffer(all_pages.get(i));

        }
        return temp;
    }

    //Evicting the slotted page from the memory buffer
    public void evict() throws IOException
    {
        //String table_name = slotted_pages.get(0).table_name;
        //Integer page_no = slotted_pages.get(0).page_no;


        //is_available_page.remove(new TablePage(table_name,page_no));

        //The slotted page at 0th index will be the least recently used.
        flush(slotted_pages.get(0));

        Main.log_writer.write("SWAP OUT T-"+slotted_pages.get(0).table_name+" P-"+slotted_pages.get(0).page_no);
        Main.log_writer.write("\n");
        slotted_pages.remove(0);
    }

    //Load the slotted page from the disk to the memory
    //LoadPageToDisk is actually LoadPageToBuffer
    public void loadPageToDisk(String table_name,Integer page_no) throws IOException
    {
        File slotted_file = new File("Database//"+table_name+"//"+table_name+page_no+".txt");

        if(slotted_file.createNewFile())
        {
            //System.out.println("Slotted file successfully created");
        }

        SlottedPage new_page = new SlottedPage(table_name, page_no);

        BufferedReader slotted_reader = new BufferedReader(new FileReader(slotted_file));
        String line = "";
        while((line = slotted_reader.readLine()) != null)
        {
            String[] records = line.split("#");
            for(String temp: records)
            {
                //records_with_meta[0] has the number of bytes
                //records_with_meta[1] is free or not
                //records_with_meta[2] is the tuple(space separated)
                String[] records_with_meta = temp.split("/");

                //Remove all the extra spaces
                records_with_meta[2] = records_with_meta[2].replaceAll("\\s+", " ");
                String[] tuple  = records_with_meta[2].split(" ");
                RecordMeta add_to_slotted_page = new RecordMeta(Integer.valueOf(records_with_meta[0])
                                                        ,Integer.valueOf(records_with_meta[1]),
                                                new Record(Integer.valueOf(tuple[0]),tuple[1],tuple[2]));

                new_page.cur_size +=records_with_meta[0].length()+1
                                        +records_with_meta[1].length()+1
                                            +records_with_meta[2].length()+1;

                new_page.contents.add(add_to_slotted_page);
                //Update the ID hashmap for this slotted page
                new_page.id_map.put(Integer.valueOf(tuple[0]),1);

            }

        }
        //new_page.print_slotted_page_contents();
        add_to_buffer(new_page);


    }

    public void flushAllPages() throws IOException
    {
        for(SlottedPage temp:slotted_pages)
        {
            flush(temp);
        }
    }

    //Update the contents from the buffer to the disk
    public void flush(SlottedPage flushed_page) throws IOException
    {
        //Check if the directory is present.
        //if yes go to page number and check it if there or not
        //Overwrite the contents
        String table_name = flushed_page.table_name;
        Integer page_no = flushed_page.page_no;

        File flush_f = new File("Database//"+table_name+"//"+table_name+page_no+ ".txt");
        if(flush_f.createNewFile())
        {
            System.out.println("File just now created");
        }
        else
        {
            if(flush_f.delete())
            {
                flush_f = new File("Database//"+table_name+"//"+table_name+page_no+ ".txt");
                flush_f.createNewFile();
            }
            else
            {
                System.out.println("File exists but problems with deletion!");
            }
        }

        BufferedWriter flush_writer = new BufferedWriter(new FileWriter(flush_f));
        for(RecordMeta temp: flushed_page.contents)
        {

            if(temp.isFree == 0)
            {
                System.out.println("+++++++Flush delted record");
                System.out.println(temp.numberOfBytes+"/"+temp.isFree+"/"+temp.record.ID+" "+temp.record.clientName+" "+temp.record.phoneNumber+"#");
            }

            //Check if isfree is zero then dont flush
            if(temp.isFree == 1)
                flush_writer.write(temp.numberOfBytes+"/"+temp.isFree+"/"+temp.record.ID+" "+temp.record.clientName+" "+temp.record.phoneNumber+"#");

            //flush_writer.write("\n");
        }
        flush_writer.close();

    }

    public void print_slottedPages_in_buffer()
    {
        System.out.println("Buffer contents size " + slotted_pages.size());
        for(SlottedPage temp:slotted_pages)
        {
            temp.print_slotted_page_contents();
        }
    }


}
