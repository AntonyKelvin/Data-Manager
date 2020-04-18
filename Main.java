import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class TableID
{
    String table_name;
    Integer ID;

    public TableID(String table_name,Integer ID)
    {
        this.table_name = table_name;
        this.ID = ID;
    }
}

public class Main {

    //This hashmap stores the last slotted page for each table
    public static HashMap<String,Integer>table_lastPageno = new HashMap<String,Integer>();

    //This hashmap stores the table name with the corresponding ID
    public static HashMap<String, ArrayList<Integer>>table_id = new HashMap<String,ArrayList<Integer>>();


    public static BufferedWriter log_writer;

    static {
        try {
            log_writer = new BufferedWriter(new FileWriter("logger.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //DROPPPING THIS FOR NOW :This hashmap stores which ID of a table belongs to which slotte page
    //public static HashMap<TableID,Integer>tableID_pageno = new HashMap<TableID,Integer>();

    public static Memory main_mm= new Memory();


    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }


    //Read metadata.txt file and update the hash map
    public static void load_meta_data() throws IOException
    {
        File meta_file =  new File("metadata.txt");
        BufferedReader read_meta = new BufferedReader(new FileReader(meta_file));

        String line = "";
        while( (line = read_meta.readLine()) != null)
        {
            //table_page[0] will have the table name
            //table_page[1] will have the last page
            String[] table_page = line.trim().split(" ");
            table_lastPageno.put(table_page[0],Integer.valueOf(table_page[1]));
        }
    }

    public static void load_id_data() throws IOException
    {
        File meta_file =  new File("metadata.txt");
        BufferedReader read_meta = new BufferedReader(new FileReader(meta_file));

        String line = "";
        while( (line = read_meta.readLine()) != null)
        {
            //table_page[0] will have the table name
            //table_page[1] will have the ID
            String[] table_page = line.trim().split(" ");

            if(table_id.containsKey(table_page[0]))
            {
                table_id.get(table_page[0]).add(Integer.valueOf(table_page[1]));
            }
            else
            {
                ArrayList<Integer>temp = new ArrayList<Integer>();
                temp.add(Integer.valueOf(table_page[1]));
                table_id.put(table_page[1],temp);
            }
        }
    }

    public static  void store_id_data() throws IOException
    {
        //Delete the id file and again create it
        //and write the contents of the hashmap to the file
        File meta_file =  new File("id.txt");
        if(meta_file.delete())
        {
            meta_file = new File("id.txt");
            meta_file.createNewFile();
        }

        BufferedWriter write_meta = new BufferedWriter(new FileWriter("id.txt"));
        Iterator table_lastPageno_iterator = table_id.entrySet().iterator();

        while(table_lastPageno_iterator.hasNext())
        {
            Map.Entry mapElement = (Map.Entry)table_lastPageno_iterator.next();
            ArrayList<Integer>temp = (ArrayList<Integer>)mapElement.getValue();

            for(Integer id : temp)
            {
                write_meta.write(mapElement.getKey()+" "+id);
                write_meta.write("\n");
            }

        }
        write_meta.close();
    }

    //Update all the contents in the Hashmap to the metadata.txt file.
    public static void store_meta_data() throws IOException
    {
        //Delete the metadata file and again create it
        //and write the contents of the hashmap to the file
        File meta_file =  new File("metadata.txt");
        if(meta_file.delete())
        {
            meta_file = new File("metadata.txt");
            meta_file.createNewFile();
        }

        BufferedWriter write_meta = new BufferedWriter(new FileWriter("metadata.txt"));
        Iterator table_lastPageno_iterator = table_lastPageno.entrySet().iterator();

        while(table_lastPageno_iterator.hasNext())
        {
            Map.Entry mapElement = (Map.Entry)table_lastPageno_iterator.next();
            write_meta.write(mapElement.getKey()+" "+mapElement.getValue());
            write_meta.write("\n");
        }
        write_meta.close();

    }
    /*
    public static void load_meta_slotted()
    {

    }

    public static void store_meta_slotted()
    {

    }

     */

    public static void print_table_id()
    {
        Iterator table_id_iterator = table_id.entrySet().iterator();

        System.out.println("_______________ID hashmap contents________________");
        while(table_id_iterator.hasNext())
        {
            Map.Entry mapElement = (Map.Entry)table_id_iterator.next();
            ArrayList<Integer>temp = (ArrayList<Integer>)mapElement.getValue();

            for(Integer id : temp)
            {
                System.out.println(mapElement.getKey()+" "+id);
            }

        }
        System.out.println("___________________________________________________");

    }

    public static boolean check_id_exists(String table_name,Integer ID)
    {
        if(check_table_exists(table_name))
        {
            if(table_id.get(table_name).contains(ID))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean check_table_exists(String table_name)
    {
        if(table_lastPageno.containsKey(table_name))
        {
            return true;
        }
        return false;
    }

    //Read script.txt and perform operations
    public static void readScript() throws IOException
    {
        System.out.println("Initially");
        print_table_id();
        System.out.println("Initially");

        load_meta_data();

        load_id_data();

        File read_script = new File("script.txt");
        BufferedReader r_script = new BufferedReader(new FileReader(read_script));

        //Create a bufferedwriter for log files


        String line = "";
        while( (line = r_script.readLine()) != null)
        {
            //perform_operation[0] -> "R","M","W","E","D"
            //perform_operation[1] -> table_name
            //perform_operation[2] -> optional(tuple or a single value)
            String perform_operation = line.trim();

            if(perform_operation.charAt(0) == 'R')
            {
                //Search for that ID in buffer. If present that returns the index in the buffer.
                //else IDsearchInDIsk and that would bring it to the buffer if present.


                perform_operation = perform_operation.replaceAll("\\s+", " ");

                //operation_meta[0] will have operation ID
                //operation_meta[1] will have the table name
                //operation_meta[2] will have the ID
                String[] operation_meta = perform_operation.split(" ");

                log_writer.write("R " + operation_meta[1] + " " + operation_meta[2]);
                log_writer.write("\n");

                int index = main_mm.isIDinBuffer(operation_meta[1],Integer.valueOf(operation_meta[2]));

                if(index == -1)
                {
                    if(main_mm.IdSearchInDisk(operation_meta[1],Integer.valueOf(operation_meta[2]),table_lastPageno.get(operation_meta[1])))
                    {
                        index = main_mm.isIDinBuffer(operation_meta[1],Integer.valueOf(operation_meta[2]));
                    }
                    else
                    {
                        System.out.println("Search results for_____" + perform_operation + "______________");
                        System.out.println("Sorry the record doesn't exists");
                        System.out.println("Search results for_____" + perform_operation + "______________");
                    }
                }

                if(index != -1)
                {
                    System.out.println("Search results for_____" + perform_operation + "______________");
                    main_mm.printIDinBuffer(operation_meta[1],Integer.valueOf(operation_meta[2]),index);
                    System.out.println("Search results for_____" + perform_operation + "______________");
                }

            }
            else if(perform_operation.charAt(0) == 'M')
            {
                //Retrieve results for records matching area code
                perform_operation = perform_operation.replaceAll("\\s+", " ");


                //operation_meta[0] will have operation ID
                //operation_meta[1] will have the table name
                //operation_meta[2] will have the ID
                String[] operation_meta = perform_operation.split(" ");

                log_writer.write("M " + operation_meta[1] + " " + operation_meta[2]);
                log_writer.write("\n");

                HashMap<String,Integer>visited_pages = new HashMap<String,Integer>();
                ArrayList<RecordMeta>query_results = new ArrayList<RecordMeta>();


                //Visited pages will have all the pages that contain this area code so that we do not have to bring search them in disk
                visited_pages = main_mm.searhAreaInBuffer(operation_meta[1],operation_meta[2],query_results);
                //System.out.println("Visited pages size " + visited_pages.size());

                //Search in disk
                main_mm.areaSearchInDisk(operation_meta[1],operation_meta[2],table_lastPageno.get(operation_meta[1]),query_results,visited_pages);

                //Print all the results it is in the query_results
                System.out.println("Search results for_____" + perform_operation + "_______________");
                for(RecordMeta result: query_results)
                {
                    System.out.println(result.record.ID+" "+result.record.clientName+" "+result.record.phoneNumber);
                    log_writer.write("Mread: "+result.record.ID+","+result.record.clientName+","+result.record.phoneNumber);
                    log_writer.write("\n");
                }
                System.out.println("Search results for_____" + perform_operation + "_______________");

            }
            else if(perform_operation.charAt(0) == 'W')
            {
                //Insert or update the record.
                //Bring the last slotted page to the buffer and then write the contents

                //We are removing the unnecessary () in the beginning and the end
                //tuple[0] holds the ID
                //tuple[1] holds the ClientName
                //tuple[2] holds the PhoneNumber
                //Make sure you trim() before sending it to any function



                String table_name = String.valueOf(perform_operation.charAt(2));
                String[] tuple = perform_operation.substring(5,perform_operation.length()-1).trim().split(",");


                //I will check if I need to update if yes then do the update and continue
                //else just do the insert part

                if(!check_table_exists(table_name))
                {
                    //This is a new table so just create the directory for that table

                    if(new File("Database//"+table_name).mkdir())
                        //System.out.println("Directory created successfully");
                    table_lastPageno.put(table_name,1);
                    ArrayList<Integer>temp = new ArrayList<Integer>();
                    table_id.put(table_name,temp);

                    //First slotted page creation
                    log_writer.write("CREATE T-"+table_name+" P-1");
                    log_writer.write("\n");
                }

                //print_table_id();
                System.out.println(perform_operation);
                System.out.println("_"+tuple[0]+"_");
                if(table_id.get(table_name).contains(Integer.valueOf(tuple[0])))
                {
                    //It is an update operation
                    print_table_id();
                    System.out.println("It is an update operation " + table_name + " " + tuple[0]);
                    int index = main_mm.isIDinBuffer(table_name,Integer.valueOf(tuple[0]));
                    if(index == -1)
                    {
                        //Then that ID is not in the buffer so search for that ID in the Disk
                        //It will put that page in the disk check if it is deleted or not in the disk
                        //if not put it in the buffer and return the index number in the buffer
                        if(!main_mm.IdSearchInDisk(table_name,Integer.valueOf(tuple[0]),table_lastPageno.get(table_name)))
                        {
                            System.out.println("Invalid update since record is already deleted");
                            continue;
                        }
                        index = main_mm.cur_size-1;

                    }
                        //tuple[0] is the ID
                        //tuple[1] is the clientName
                        //tuple[2] is the phoneNumber
                        String whole_record = tuple[0].trim()+" "+tuple[1].trim()+" "+tuple[2].trim();
                        Record update_record_tuple = new Record(Integer.valueOf(tuple[0]),tuple[1].trim(),tuple[2].trim());

                        String content = (whole_record.length()+2)+"/"+1+"/"+whole_record;
                        RecordMeta update_record = new RecordMeta(whole_record.length()+2,1,update_record_tuple);
                        main_mm.update_record_in_buffer(index,Integer.valueOf(tuple[0]),update_record);

                    continue;
                }



                Record add_record = new Record(Integer.valueOf(tuple[0].trim()),tuple[1].trim(),tuple[2].trim());
                if(add_record.isValidRecord())
                {
                    //Add the record to the slotted page
                    Integer cur_page_no = table_lastPageno.get(table_name);
                    SlottedPage new_slotted_page;

                    int index = main_mm.isInBuffer(table_name,cur_page_no);
                    String whole_record = tuple[0].trim()+" "+tuple[1].trim()+" "+tuple[2].trim();
                    String content = (whole_record.length()+2)+"/"+1+"/"+whole_record;
                    if(index == -1)
                    {
                        //It is not in the buffer have to bring it from the disk
                        main_mm.loadPageToDisk(table_name,cur_page_no);

                        //System.out.println("Last page is at " + (main_mm.cur_size - 1) + " " + main_mm.max_size);
                        SlottedPage new_page = main_mm.getPage(main_mm.cur_size-1);

                        //Add the record to the slotted page
                        //System.out.println("Whole content to add to slotted page " + content);
                        if(new_page.is_add_record(content+"#"))
                        {
                            new_page.add_record_to_slottedpage(content);
                        }
                        else
                        {
                            //Update the hashmap
                            table_lastPageno.put(table_name,table_lastPageno.get(table_name)+1);
                            cur_page_no++;
                            log_writer.write("CREATE T-"+table_name+" P-"+cur_page_no);
                            log_writer.write("\n");

                            //Load the page from disk to buffer
                            main_mm.loadPageToDisk(table_name,cur_page_no);
                            SlottedPage cur_page_slotted = main_mm.getPage(main_mm.cur_size-1);
                            cur_page_slotted.add_record_to_slottedpage(content);
                        }
                        table_id.get(table_name).add(Integer.valueOf(tuple[0]));
                    }
                    else
                    {
                        //I need to get that page from the index. and then I need to add the record(check size)
                        //and add to the buffer again I need to make it recent.

                        SlottedPage check_page = main_mm.getPage(index);
                        if(check_page.is_add_record(content+"#"))
                        {
                            check_page.add_record_to_slottedpage(content);
                            main_mm.add_to_buffer(check_page);
                        }
                        else
                        {
                            //This is a reused code to create a new page because the exisiting is full
                            //and add it to buffer.
                            table_lastPageno.put(table_name,table_lastPageno.get(table_name)+1);
                            cur_page_no++;
                            log_writer.write("CREATE T-"+table_name+" P-"+cur_page_no);
                            log_writer.write("\n");

                            //Load the page from disk to buffer
                            main_mm.loadPageToDisk(table_name,cur_page_no);
                            SlottedPage cur_page_slotted = main_mm.getPage(main_mm.cur_size-1);
                            cur_page_slotted.add_record_to_slottedpage(content);
                        }
                        table_id.get(table_name).add(Integer.valueOf(tuple[0]));
                    }

                }
                else
                {
                    System.out.println("Not a valid record to add!");
                }



                //Write to the log in the end
            }
            else if(perform_operation.charAt(0) == 'E')
            {

                //Replace this with everything
                perform_operation = perform_operation.replaceAll("\\s+", " ");

                //operation_meta[0] will have operation ID
                //operation_meta[1] will have the table name
                //operation_meta[2] will have the ID
                String[] operation_meta = perform_operation.split(" ");
                log_writer.write("E "+operation_meta[1]+" "+operation_meta[2]);
                log_writer.write("\n");

                //If it is in the buffer :
                // I need to make its free as 0 and update the ID hashmap and then write to a meta file regarding this hole.
                //else:
                //Bring it to buffer and do the same

                //You need to make this page most recent
                System.out.println("_____________________Delete operation________________________");
                int index = main_mm.isIDinBuffer(operation_meta[1],Integer.valueOf(operation_meta[2]));
                if(index == -1)
                {
                    //Then it is not in the buffer
                    System.out.println("Deleted record was not in buffer");
                    System.out.println(operation_meta[1]+" "+operation_meta[2]+" "+table_lastPageno.get(operation_meta[1]));

                    main_mm.IdSearchInDisk(operation_meta[1],Integer.valueOf(operation_meta[2]),table_lastPageno.get(operation_meta[1]));
                    index = main_mm.cur_size-1;
                }


                main_mm.delete_record_in_buffer(index,Integer.valueOf(operation_meta[2]));

                //Update ID hashmap here(value)
                System.out.println(operation_meta[0]+" "+operation_meta[1]+" "+operation_meta[2]);
                table_id.get(operation_meta[1]).remove(Integer.valueOf(operation_meta[2]));
                System.out.println("_____________________Delete operation________________________");
                log_writer.write("Erased: "+operation_meta[1] + " " + operation_meta[2]);
                log_writer.write("\n");

            }
            else if(perform_operation.charAt(0) == 'D')
            {
                perform_operation = perform_operation.replaceAll("\\s+", " ");

                //operation_meta[1] has the table name
                String[] operation_meta = perform_operation.split(" ");
                log_writer.write("D "+operation_meta[1]);
                log_writer.write("\n");

                File delete_f = new File("Database//"+operation_meta[1]);

                //If the table does not exists
                if(!table_lastPageno.containsKey(operation_meta[1]))
                {
                    System.out.println("Table does not exists");
                }
                else
                {
                    deleteDir(delete_f);
                    table_lastPageno.remove(operation_meta[1]);
                    //Update the ID hashmap(Key)
                    table_id.remove(operation_meta[0]);

                    main_mm.delete_from_buffer(operation_meta[1]);
                }

                log_writer.write("Deleted: " + operation_meta[1]);
                log_writer.write("\n");

            }
            else
            {
                System.out.println("The operation doesn't exists");
            }
        }

        //print_table_id();

        store_meta_data();
        store_id_data();



        System.out.println("________________CONTENTS IN BUFFER ________________");
        main_mm.print_slottedPages_in_buffer();
        System.out.println("___________________________________ ________________");
        main_mm.flushAllPages();
    }
    public static void main(String[] args) throws IOException
    {
        //Create the log file
        //We will assume the meta data file is already created

        File log_file = new File("logger.txt");
        /*
        if(log_file.createNewFile())
        {
            System.out.println("Log files created successfully");
        }
        else
        {
            System.out.println("Log file already exists");
        }

         */

        readScript();
        log_writer.close();
    }
}
