import org.osbot.rs07.accessor.XNPC;
import org.osbot.rs07.api.Bank.BankMode;
import org.osbot.rs07.api.model.Character;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@ScriptManifest(name = "Potion maker", author = "dokato", version = 2.65, info = "", logo = "") 
public class Main extends Script {
	
	private String rsn;
	
	private static final Color customRed = new Color(255, 0, 0, 150);
	private static final Color customGreen = new Color(0, 255, 51, 150);
	private static final Color standardTxtColor = new Color(255, 255, 255);
	
	private boolean tradeSwitch1;
	private boolean tradeSwitch2;
	private boolean tradeSwitch3;
	private static final Rectangle tradeRect1 = new Rectangle(450, 8, 20, 20);
	private static final Rectangle tradeRect2 = new Rectangle(420, 8, 20, 20);
	private static final Rectangle tradeRect3 = new Rectangle(390, 8, 20, 20);
	
	private boolean tradeSwitch4;
	private boolean tradeSwitch5;
	private boolean tradeSwitch6;
	private static final Rectangle tradeRect4 = new Rectangle(450, 36, 20, 20);
	private static final Rectangle tradeRect5 = new Rectangle(420, 36, 20, 20);
	private static final Rectangle tradeRect6 = new Rectangle(390, 36, 20, 20);
	
	private static final int VIAL_OF_WATER_ID = 227;
	private static final int VIAL_OF_WATER_NOTE_ID = 228;
	
	private List<PotItem> allPotItems;
	private int[] potItemIdsNoNote;
	private int[] potItemIdsNote;
	
	private PotItem potItem;
	
	private long potsInBank;
	private long potsToGo;
	
	private boolean startb = true;
	
    private long timeRan;
    private long timeBegan;
    
    // 10 mins
    private final long TIME_TO_WAIT_FOR_MULER = 8 * 60000;
    private long timeToWaitForMulerRndmized;
    private int waitTimeInMins;
    private long timeStartWaiting;
    private boolean waiting;
	
	private String status;
	
	private boolean doneBanking;
	
	private String muler;
	
	private boolean sendTradeRequest;
	private boolean tradeAccepted;
	
	@Override
    public void onStart(){
		this.timeBegan = System.currentTimeMillis();
		setPotItems();
		setMuler();
		potItemIdsNoNote = getAllPotItemIdsNoNoteArr();
		potItemIdsNote = getAllPotItemIdsNoteArr();
		potsInBank = 0;
		potsToGo = 0;
		randomizeWaitTillLogOutTime();
		
		getBot().addMouseListener(new MouseListener() {
			
			public void mousePressed(MouseEvent e) {
				if(tradeRect1.contains(e.getPoint()))
					tradeSwitch1 = !tradeSwitch1;
				if(tradeRect2.contains(e.getPoint()))
					tradeSwitch2 = !tradeSwitch2;
				if(tradeRect3.contains(e.getPoint()))
					tradeSwitch3 = !tradeSwitch3;
				
				if(tradeRect4.contains(e.getPoint()))
					tradeSwitch4 = !tradeSwitch4;
				if(tradeRect5.contains(e.getPoint()))
					tradeSwitch5 = !tradeSwitch5;
				if(tradeRect6.contains(e.getPoint()))
					tradeSwitch6 = !tradeSwitch6;
			}
			
			public void mouseReleased(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
		});
		
		rsn = getBot().getUsername().substring(0, getBot().getUsername().indexOf('@'));
    }
    
    private void randomizeWaitTillLogOutTime() {
    	timeToWaitForMulerRndmized = 
    			(long)(TIME_TO_WAIT_FOR_MULER * (1 + (Math.random() - 0.4)));
    	
    	waitTimeInMins = (int) (timeToWaitForMulerRndmized / 60000);
	}

	private void setMuler() {
    	try {
    		status="Getting muler";
    		muler = PotSettings.getMuler();
		} catch (Exception e) {
			log("problem getting muler");
			log(e.getMessage());
			stop();
		}
	}

	private void setPotItems() {
    	try {
    		status="Getting potItem";
    		allPotItems = PotSettings.getPotItems();
    		status="setting first potitem";
    		potItem = allPotItems.get(0);
		} catch (Exception e) {
			log("problem getting potItems");
			log(e.getMessage());
			stop();
		}
	}

	@Override
    public void onExit() {
    }


    @Override
    public int onLoop() throws InterruptedException{
    	status="loop started";
    	procedures();    	
    	
    	if(needsToTrade() || needsToSupplyTrade()){
    		tradeProcedures();
    	}else{
    		if(readyToMakePots()){
        		makePots();
        	}else{
        		bank();
        	}
    	}
    	
    	return random(59,215);
    }
	private boolean needsToSupplyTrade() {
    	return tradeSwitch4 && tradeSwitch5 && tradeSwitch6;
	}

	private void tradeProcedures() throws InterruptedException {
    	if(tradeAccepted && !getTrade().isCurrentlyTrading())
    		resetTradeStuff();
    	
    	status="cheking if has all stuff from bank";
		if(doneBanking){
			status="checking if ban is open 2";
			if(getBank().isOpen()){
				status="closing bank 2";
				getBank().close();
			}else{
				tradeWithMuler();
			}
		}else{
			bankForTrade();
		}
	}
    
    private void resetTradeStuff(){
    	setTradeSwitchesOFF();
    	setSupplySwitchesOFF();
		tradeAccepted = false;
		doneBanking = false;
		sendTradeRequest = false;
    }

	private void tradeWithMuler() throws InterruptedException {
		status="checking if currently in trade";
		if(getTrade().isCurrentlyTrading()){
			sendTradeRequest = false;
			status = "currently trading";
			if(getTrade().isFirstInterfaceOpen()){
				status = "first trade window ";
				if(getInventory().isEmpty() && getTrade().didOtherAcceptTrade()){
					status = "Accepting..";
    				getTrade().acceptTrade();
    				sleep(random(860,1241));
				}else{
					status="checks if need to supply trade";
					if(!needsToSupplyTrade()){
						status="checks if inv is empty for trade";
						if(!getInventory().isEmpty()){
							status = "putting items in trade";
							for(Item item : getInventory().getItems()){
								status="offering all";
								// anders exception ...
								if(getInventory().contains(item.getId())){
									getInventory().getItem(item.getId()).interact("Offer-All");
									sleep(random(250, 780));
								}
							}
						}
					}
				}
			}else if(getTrade().isSecondInterfaceOpen()){
    			status = "second trade window";
    			if(getTrade().didOtherAcceptTrade()){
    				getTrade().acceptTrade();
    				sleep(random(480,860));
    				tradeAccepted = true;
    			}
    		}
		}else{
			status="about to get the muler";
			Player mule = getPlayer(muler,3);
			status="checking if muler != null";
			if(mule != null){
				resetWaitStuff();
				status="check if i need to send trade request";
				if(!sendTradeRequest && !mule.isMoving()){
					status="about to interact with muler";
					mule.interact("Trade with");
					sleep(random(970,1456));
				}
			}else{
				waitSomeTimeAndStop();
			}
		}
		
	}
	
	private void resetWaitStuff() {
		status="resetting wait stuff";
		waiting = false;
	}

	private void waitSomeTimeAndStop() {
		if(waiting){
			status="waiting on muler";
			if(isWaitingLongerThanXmins()){
				stop();
			}
		}else{
			status="setting up wait stuff";
			waiting = true;
			timeStartWaiting = System.currentTimeMillis();
		}
	}

	private boolean isWaitingLongerThanXmins() {
		return (System.currentTimeMillis() - timeStartWaiting) > timeToWaitForMulerRndmized;
	}

	private Player getPlayer(String name, int radius){
		status = "getting the muler";
		return getPlayers().closest(myPlayer().getArea(radius), muler);
    }

	private boolean hasStufInInv() {
		status="Checkign if has the stuff in inv";
		return !getInventory().isEmpty() && getInventory().contains(potItemIdsNote);
	}

	private void bankForTrade() throws InterruptedException {
		status="checking if bank is open 3";
		if(getBank().isOpen()){
			if(getInventory().isEmpty() || getInventory().contains(potItemIdsNote)){
				if(needsToSupplyTrade()){
					doneBanking = true;
				}else{
					status="about to iterate over all potitemIds";
					for(int id : potItemIdsNoNote){
						status="iterating over all opitemIds";
						if(getBank().contains(id)){
							status="checking if note mode is on";
							if(getBank().getWithdrawMode().equals(BankMode.WITHDRAW_NOTE)){
								status="withdrawing potitem";
								getBank().withdrawAll(id);
							}else{
								status="enabling note mode";
								getBank().enableMode(BankMode.WITHDRAW_NOTE);
							}
						}
					};
					if(!getBank().contains(potItemIdsNoNote))
						doneBanking = true;
				}
			}else{
				getBank().depositAll();
			}
		}else{
			getBank().open();
		}
	}
	
	private int[] getAllPotItemIdsNoNoteArr() {
		status="getAllPotItemIdsNoNote()";
		return addElement(
				Stream.concat(
				allPotItems
				.stream()
				.map(p -> p.pot_id),
				allPotItems
				.stream()
				.map(p -> p.herb_id)
				)
				.mapToInt(i -> i.intValue())
				.toArray(),
				VIAL_OF_WATER_ID
				);	
	}
	
	private int[] getAllPotItemIdsNoteArr() {
		status="getAllPotItemIds()";
		return addElement(
				Stream.concat(
				allPotItems
				.stream()
				.map(p -> p.pot_note_id),
				allPotItems
				.stream()
				.map(p -> p.herb_note_id)
				)
				.mapToInt(i -> i.intValue())
				.toArray(),
				VIAL_OF_WATER_NOTE_ID
				);	
	}
	
	static int[] addElement(int[] a, int e) {
	    a  = Arrays.copyOf(a, a.length + 1);
	    a[a.length - 1] = e;
	    return a;
	}

	private boolean needsToTrade(){
    	return tradeSwitch1 && tradeSwitch2 && tradeSwitch3;
    }
    
    private void setTradeSwitchesOFF(){
		tradeSwitch1 = false;
		tradeSwitch2 = false;
		tradeSwitch3 = false;
	}
    
    private void setTradeSwitchesOn(){
		tradeSwitch1 = true;
		tradeSwitch2 = true;
		tradeSwitch3 = true;
	}
    
    private void setSupplySwitchesOFF(){
		tradeSwitch4 = false;
		tradeSwitch5 = false;
		tradeSwitch6 = false;
	}
    
    private void setSupplySwitchesOn(){
		tradeSwitch4 = true;
		tradeSwitch5 = true;
		tradeSwitch6 = true;
	}
    
    private void procedures() {
    	status="checking yaw angle";
    	if(getCamera().getYawAngle() == 0){
    		status="moving yaw";
    		getCamera().moveYaw(random(200,330));
    	}
    	status="checking pitch angle";
    	if(getCamera().getPitchAngle() > 55){
    		status="moving pitch";
    		getCamera().movePitch(random(21,48));
    	}
	}

	private void makePots() throws InterruptedException {
    	status="checking if bank is still open";
    	if(getBank().isOpen()){
    		status="closing bank";
    		getBank().close();
    		sleep(random(12,217));
    	}else{
    		status="checking if i'm animating";
    		if(!myPlayer().isAnimating()){
    			if(isDialogOpen()){
    				status="about to click on the make potion button";
    				getWidgets().get(270, 14, 29).interact("Make");
    				sleep(random(940,1748));
    			}else{
    				status="checking if there is an item selected in inv";
        			if(getInventory().isItemSelected()){
        				status="checking if selected item is the ingredient itself";
    	    			if(getInventory().getSelectedItemName().equals(getInventory().getItem(potItem.herb_id).getName())){
    	    				status="about to use the ingredient on the vial ";
    	    				getInventory().getItem(VIAL_OF_WATER_ID).interact("Use");
    	    				sleep(random(1236,1874));
    	    			}
        			}else{
        				status="about to click on the ingredient";
        				getInventory().getItem(potItem.herb_id).interact("Use");
        				sleep(random(120,412));
        			}
    			}
    		}else{
    			hoverSomewhere();
    			status="waiting";
    			sleep(random(2789,4987));
    		}
    	}
	}

	private void hoverSomewhere() {
	}

	private boolean isDialogOpen() {
		status="checking if is in the dialogue";
		return getDialogues().inDialogue() && !getDialogues().isPendingContinuation();
	}

	private void bank() throws InterruptedException {
		deselectItem();
    	status="checking if bank is open";
    	if(getBank().isOpen()){
    		if(getInventory().isEmpty() ){
    			boolean hasHerb = false;
    			if(!hasIngredientInInv()){
        			status="checking if has ingredients in bank";
        			if(!getBank().contains(potItem.herb_id)){
        				status="no ingredients in bank, setting new PotItem";
        				setNewPotItem();
        			}else{
            			status="withdrawing ingredients from bank";
            			getBank().withdraw(potItem.herb_id, 14);
            			sleep(random(57,259));
            			hasHerb = true;
        			}
        		}
    			if(!hasVialInInv() && hasHerb){
        			status="checking if has vials  in bank";
        			if(!getBank().contains(VIAL_OF_WATER_ID)){
        				status="no vials  in bank, need to trade";
        				setTradeSwitchesOn();
        			}else{
            			status="withdrawing vials  from bank";
            			getBank().withdraw(VIAL_OF_WATER_ID, 14);
            			sleep(random(57,289));
        			}
        		}
    		}else{
    			getBank().depositAll();
    			incrementPotsinBank();
    		}
    	}else{
    		status="opening bank booth";
    		getBank().open();
    	}
	}

	private void setNewPotItem() {
		status="looping over all potitems";
		for(PotItem potItm : allPotItems){
			status="ochecking if new pot item is in bank";
			if(getBank().contains(potItm.herb_id)){
				status="setting the new pot item";
				this.potItem = potItm;
				return;
			}
		}
		status="no more hebrs in bank, trading ... ";
		log("No herbs in bank");
		setTradeSwitchesOn();;
	}

	private Character<XNPC> getBanker() {
		return getNpcs().closest("Banker");
	}

	private void incrementPotsinBank() {
		if(getBank().contains(potItem.pot_id)){
			potsInBank = getBank().getAmount(potItem.pot_id);
		}
		if(getBank().contains(potItem.herb_id)){
			potsToGo = getBank().getAmount(potItem.herb_id);
		}
	}

	private void deselectItem() throws InterruptedException {
		if(getInventory().isItemSelected()){
			getInventory().deselectItem();
			sleep(random(54,123));
		}
	}

	private boolean readyToMakePots() {
		status="Checking if has good setup inv";
    	return hasVialInInv() && hasIngredientInInv();
	}

	private boolean hasIngredientInInv() {
		status="checking if has ingredient in inv";
		return getInventory().contains(potItem.herb_id);
	}

	private boolean hasVialInInv() {
		status="checking if has water vials in inv";
		return getInventory().contains(VIAL_OF_WATER_ID);
	}

	@Override
    public void onPaint(Graphics2D g1){
    	
    	if(this.startb){
    		this.startb=false;
    		this.timeBegan=System.currentTimeMillis();
    	}
    	this.timeRan = (System.currentTimeMillis() - this.timeBegan);
		
		Graphics2D g = g1;

		int startY = 55;
		int increment = 15;
		int value = (-increment);
		int x = 20;
		
		g.setFont(new Font("Arial", 0, 13));
		g.setColor(standardTxtColor);
		g.drawString("Acc: " + rsn, x,getY(startY, value+=increment));
		g.drawString("World: " + getWorlds().getCurrentWorld(),x,getY(startY, value+=increment));
		value+=increment;
		g.drawString("Version: " + getVersion(), x, getY(startY, value+=increment));
		g.drawString("Runtime: " + ft(this.timeRan), x, getY(startY, value+=increment));
		g.drawString("Status: " + status, x, getY(startY, value+=increment));
		value+=increment;
		g.drawString("Current pot: " + potItem.pot_name, x, getY(startY, value+=increment));
		value+=increment;
		g.drawString("Pots done in bank: " + potsInBank, x, getY(startY, value+=increment));
		g.drawString("Pots to go: " + potsToGo, x, getY(startY, value+=increment));
		value+=increment;
		g.drawString("Wait time on muler : " + waitTimeInMins , x, getY(startY, value+=increment));
		value+=increment;
		g.drawString("Hebrlore lvl: " + getSkills().getStatic(Skill.HERBLORE), x, getY(startY, value+=increment));		
		g.drawString("Exp to next lvl: " + getExpToNextLevel(Skill.HERBLORE), x, getY(startY, value+=increment));		

		if(tradeSwitch1) g.setColor(customGreen);
		else g.setColor(customRed);
		fillRect(g, tradeRect1);
		
		if(tradeSwitch2)g.setColor(customGreen);
		else g.setColor(customRed);
		fillRect(g, tradeRect2);
		
		if(tradeSwitch3) g.setColor(customGreen);
		else g.setColor(customRed);
		fillRect(g, tradeRect3);
		
		
		if(tradeSwitch4) g.setColor(customGreen);
		else g.setColor(customRed);
		fillRect(g, tradeRect4);
		
		if(tradeSwitch5)g.setColor(customGreen);
		else g.setColor(customRed);
		fillRect(g, tradeRect5);
		
		if(tradeSwitch6) g.setColor(customGreen);
		else g.setColor(customRed);
		fillRect(g, tradeRect6);
		
    }
    
    private int getExpToNextLevel(Skill skill) {
    	int currentLevel = getSkills().getStatic(skill);
    	int nextLevel = currentLevel + 1;
    	return (getSkills().getExperienceForLevel(nextLevel) - getSkills().getExperience(skill));
	}

	private int getY(int startY, int value){
		return startY + value;
	}
    
    private void fillRect(Graphics2D g, Rectangle rect){
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
    
    public void onMessage(Message message){
    	String txt = message.getMessage().toLowerCase();
    	
    	if(txt.contains("sending trade"))
			sendTradeRequest = true;
    }
    
	private String ft(long duration) {
		String res = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration)
				- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
						.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
						.toMinutes(duration));
		if (days == 0L) {
			res = hours + ":" + minutes + ":" + seconds;
		} else {
			res = days + ":" + hours + ":" + minutes + ":" + seconds;
		}
		return res;
	}
}