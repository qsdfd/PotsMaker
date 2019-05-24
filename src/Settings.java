import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Settings {
	
	private static final String CLOUDANT_URL = "https://kaisumaro.cloudant.com/pots/pot";
			
	public static int getIngredientId(int currentHerbLvl) throws NumberFormatException, Exception {
		if(getRequiredLevel() <= currentHerbLvl)
			return Integer.parseInt(getFromDoc("herb_id", CLOUDANT_URL));
		else
			return 221;
			//return Integer.parseInt(getFromDoc("eye_of_newt_id", CLOUDANT_URL));
	}

	public static int getVialId(int currentHerbLvl) throws NumberFormatException, Exception {
		if(getRequiredLevel() <= currentHerbLvl)
			return Integer.parseInt(getFromDoc("vial_of_water_id", CLOUDANT_URL));
		else
			return 91;
			//return Integer.parseInt(getFromDoc("guam_pot_id", CLOUDANT_URL));
	}

	public static int getPotId(int currentHerbLvl) throws NumberFormatException, Exception {
		if(getRequiredLevel() <= currentHerbLvl)
			return Integer.parseInt(getFromDoc("pot_id", CLOUDANT_URL));
		else
			return 121;
			//return Integer.parseInt(getFromDoc("attak_pot_id", CLOUDANT_URL));
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
