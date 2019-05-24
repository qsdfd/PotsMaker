
public class PotItem implements Comparable<PotItem>{
	
	public String pot_name;
	public int pot_id;
	public int herb_id;
	public int pot_note_id;
	public int herb_note_id;
	public int lvl;
	public int priority;
	
	@Override
	public String toString() {
		return "PotItem [pot_name=" + pot_name + ", pot_id=" + pot_id + ", herb_id=" + herb_id + ", pot_note_id="
				+ pot_note_id + ", herb_note_id=" + herb_note_id + ", lvl=" + lvl + ", priority=" + priority + "]";
	}

	@Override
	public int compareTo(PotItem potItem) {
		return potItem.priority < this.priority ? 1 : -1;
	}
	
	
}
