import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.osbot.rs07.api.model.Player;

public class PotSettings {
	
	public static void main(String[] args) throws Exception {
//		List<PotItem> list = refreshPotItems();
//		
//		list.forEach(p -> System.out.println(p.toString()));
		
		System.out.println();
	}
	
	private static final String CLOUDANT_URL = "https://kaisumaro.cloudant.com/pots/pot";
		
//	private static Stack<PotItem> potItems = null;
	
//	public static PotItem getPotItem(int currentHerbLvl) throws Exception{
//		
//		if(potItems == null)
//			refreshPotItems();
//		
//		if(potItems.isEmpty())
//			return null;
//			
//		PotItem potItem = potItems.pop();
//		
//		if(potItem.lvl <= currentHerbLvl)
//			return potItem;
//		
//		return getPotItem(currentHerbLvl);
//	}
	
	public static List<PotItem> getPotItems() throws Exception{
		return refreshPotItems();
	}
	
	private static List<PotItem> refreshPotItems() throws Exception {
		String str = readUrl(CLOUDANT_URL).split("\"herbs\":")[1];
		str = str.substring(0, str.length() - 2);
		
		//potItems = new Stack<>();
		
		List<PotItem> potItemsList = new ArrayList<>();
		
		for(int i=1;i<=14;i++)
			potItemsList.add(new PotItem());
		
		appendPotItemListWithProperty(potItemsList, str, "pot_name", 1);
		appendPotItemListWithProperty(potItemsList, str, "pot_id", 2);
		appendPotItemListWithProperty(potItemsList, str, "herb_id", 3);
		appendPotItemListWithProperty(potItemsList, str, "pot_note_id", 4);
		appendPotItemListWithProperty(potItemsList, str, "herb_note_id", 5);
		appendPotItemListWithProperty(potItemsList, str, "lvl", 6);
		appendPotItemListWithProperty(potItemsList, str, "priority", 7);
		
		Collections.sort(potItemsList);
		
		//potItems.addAll(potItemsList);
		
		return potItemsList;
	}
	
	private static void appendPotItemListWithProperty(
			List<PotItem> potItemsList, String strFull, String prop, int field
			) {
		String[] strs = strFull.split(prop);
		
		int i = 1;
		for(PotItem potItem : potItemsList){
			if(field == 1)
				potItem.pot_name = strs[i].split(":")[1].split(",")[0].split("\"")[1];
			else if(field == 2)
				potItem.pot_id = Integer.parseInt(strs[i].split(":")[1].split(",")[0]);
			else if(field == 3)
				potItem.herb_id = Integer.parseInt(strs[i].split(":")[1].split(",")[0]);
			else if(field == 4)
				potItem.pot_note_id = Integer.parseInt(strs[i].split(":")[1].split(",")[0]);
			else if(field == 5)
				potItem.herb_note_id = Integer.parseInt(strs[i].split(":")[1].split(",")[0]);
			else if(field == 6)
				potItem.lvl = Integer.parseInt(strs[i].split(":")[1].split(",")[0]);
			else if(field == 7)
				potItem.priority = Integer.parseInt(strs[i].split(":")[1].split("}")[0]);
			i++;
		}
	}
	
	public static String getMuler() throws Exception {
		return getFromDoc("muler", CLOUDANT_URL).split("\"")[1];
	}

	private static int getRequiredLevel() throws NumberFormatException, Exception {
		return Integer.parseInt(getFromDoc("lvl", CLOUDANT_URL));
	}
	
	private static String getFromDoc(String key, String url) throws Exception {
		String response = readUrl(url);
		response = response.substring(1, response.length()-2);
		
		List<String> splits = Arrays.asList(response.split(","));
		
		return splits
				.stream()
				.filter(
						s -> s.contains(key)
						)
				.findAny().get().split(":")[1];
	}
	
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
}
