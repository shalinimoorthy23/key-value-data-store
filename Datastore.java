package freshworks;
import java.time.LocalTime;
import java.util.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ThreadExe extends Thread 
{
	   private Thread t;
	   private String threadName;
	   
	   ThreadExe( String name) 
	   {
	      threadName = name;
	      System.out.println("Creating " +  threadName );
	   }
	   
	   public void run() 
	   {
	      System.out.println("Running " +  threadName );
	      try 
	      {
	         for(int i = 5; i > 0; i--) 
	         {
	            System.out.println("Thread: " + threadName + ", " + i);
	            Thread.sleep(50);
	         }
	      } 
	      catch (InterruptedException e)
	      {
	         System.out.println("Thread " +  threadName + " interrupted.");
	      }
	      System.out.println("Thread " +  threadName + " exiting.");
	   }
	   
	   public void start () 
	   {
	      System.out.println("Starting " +  threadName );
	      if (t == null) 
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }
}

public class Datastore 
{
	public static boolean validateKey(String str)  //constraint to check the input key is capped at 32chars 
	{
	      str = str.toLowerCase();
	      char[] charArray = str.toCharArray();
	      for (int i = 0; i < charArray.length; i++)
	      {
	         char ch = charArray[i];
	         if (!(ch >= 'a' && ch <= 'z')) 
	         {
	            return false;
	         }
	      }
	      return true;
	  }
	
	public static void create(String key,String value,int TTL,Map<String,String> map) //function to add key-value pair with Time-to-live
	{
		if(map.containsKey(key))
		{
			System.out.println("error : This key already exits");
		}
		else
		{
			boolean bool = validateKey(key);
			if(bool==true)
		    {
				if(value.length()<=(16*1024) && map.size()<=(1024*1024*1024) && key.length()<=32) //constraints to check the memory limit
				{
					LocalTime time=LocalTime.now(); //to get the present system time
					int hr=time.getHour();
					int min=time.getMinute();
					int sec=time.getSecond();
					int sec1=sec+TTL;
					int min1=min;
					int hr1=hr;
					if(sec1>60)
					{
						min1=min1+1;
						sec1=sec1%60;
					}
					if(min1>60)
					{
						hr1=hr+1;
					}
					System.out.println("Expiry time: "+hr1+":"+min1+":"+sec1);
					sec1=sec1+(hr1*3600)+(min1*60);
					value=value+"@"+sec1;
					map.put(key, value);
					System.out.println("Created successfully");
				}
				else
				{
					System.out.println("error : Memory Limit Exceeded");
				}
		    }
			else
			{
				System.out.println("error : Invalid key name : key name must contain only alphabets and no special characters or numbers");
			}
		}
	}
	
	//method override is performed as Time-to-Live is optional 
	public static void create(String key,String value,Map<String,String> map) // Function to add key-value pair
	{
		if(map.containsKey(key))
		{
			System.out.println("error : This key already exits");
		}
		else
		{
			boolean bool = validateKey(key);
			if(bool==true)
			{
			if(value.length()<=(16*1024) && map.size()<=(1024*1024*1024) && key.length()<=32) //constraints to check the memory limit
			{
				map.put(key, value);
				System.out.println("Created successfully");
			}
			else
			{
				System.out.println("error : Memory Limit Exceeded");
			}
			}
			else
			{
				System.out.println("error : Invalid key name : key name must contain only alphabets and no special characters or numbers");
			}
		}
	}
	
	public static void read(String key,Map<String,String> map) //for read process
	{

		if(map.containsKey(key)) //function used to find whether the given key already exists in the data store
		{
			String value=map.get(key);
			Pattern my_pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		    Matcher my_match = my_pattern.matcher(value);
		    boolean check = my_match.find(); 
		    if(check)
		    {
		    	String words[]=value.split("@"); 
		    	int exp=Integer.parseInt(words[1]);
		    	String val=words[0];
		    	LocalTime t=LocalTime.now();
		    	int hr=t.getHour();
		    	int min=t.getMinute();
		    	int sec=t.getSecond();
		    	sec=sec+(min*60)+(hr*3600);
		    	if(sec<exp) //comparing present time with expiry time
		    	{
		    		System.out.println("{"+key+":"+val+"}");
		    	}
		    	else
		    	{
		    		System.out.println("error : Key has expired");
		    	}
		    }
		    else
		    {
		    	System.out.println("{"+key+":"+value+"}");
		    }
		}
		else
		{
			System.out.println("error : Given key does not exist");
		}
	}
		
	public static void delete(String key,Map<String,String> map) //for deletion process
	{
		if(map.containsKey(key))
		{
			String value=map.get(key);
			Pattern my_pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		    Matcher my_match = my_pattern.matcher(value);
		    boolean check = my_match.find();
		    if(check)
		    {
		    	String words[]=value.split("@");
		    	int exp=Integer.parseInt(words[1]);
		    	LocalTime t=LocalTime.now();
		    	int hr=t.getHour();
		    	int min=t.getMinute();
		    	int sec=t.getSecond();
		    	sec=sec+(min*60)+(hr*3600);
		    	if(sec<exp)
		    	{
		    		map.remove(key);
		    		System.out.println("Key is successfully deleted");
		    	}
		    	else
		    	{
		    		System.out.println("error : Key has expired");
		    	}
		    }
		    else
		    {
		    	map.remove(key);
		    	System.out.println("Key is successfully deleted");
		    }
		}
		else
		{
			System.out.println("error : Key does not exist");
		}
	}
	
	public static void main(String args[]){  
		Map<String, String> map = Collections.synchronizedMap(new HashMap<>()); //Synchronized HashMap is used as HashMap is not a thread-safe
		map.put("chennai", "25");
		Scanner sc = new Scanner(System.in);
		//A client process is allowed to access the data store using multiple threads
		ThreadExe T1 = new ThreadExe( "Thread-1");
	    T1.start();
	      
	    ThreadExe T2 = new ThreadExe( "Thread-2");
	    T2.start();
		
		//Enter 'C' to add a key-value pair to the data store
		//Enter 'T' to add a key-value pair with Time-to-live
		//Enter 'R' to read a key-value pair from the data store
		//Enter 'D' to delete a key-value pair from the data store
	    
		String k;
		String v;
		int ttl;
	    char menu;
		do
		{
	    String input = sc.next();
	    menu = input.charAt(0);
		switch(menu)
		{
		case 'C': 
			k=sc.next();
		    v=sc.next();
			create(k,v,map);
			break;
		
		case 'T':
			k=sc.next();
			v=sc.next();
			ttl=sc.nextInt();
			create(k,v,ttl,map);
			break;
		
		case 'R':
			k=sc.next();
			read(k,map);
			break;
		
		case 'D':
			k=sc.next();
			delete(k,map);
			break;
		
		case 'Q':
			break;
		
		default: 
			System.out.println("----Invalid action on the datastore----");
        }
		}while(menu!='Q');
		sc.close();
}
}

