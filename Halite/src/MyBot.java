import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyBot {
    public static void main(String[] args) throws java.io.IOException {

        final InitPackage iPackage = Networking.getInit();
        final int myID = iPackage.myID;
        final GameMap gameMap = iPackage.map;

        Networking.sendInit("Prodigy Bot");

        while(true) {
            List<Move> moves = new ArrayList<Move>();

            Networking.updateFrame(gameMap);

            for (int y = 0; y < gameMap.height; y++) {
                for (int x = 0; x < gameMap.width; x++) {
                    final Location location = gameMap.getLocation(x, y);
                    final Site site = location.getSite();
                    if(site.owner == myID) {
                        moves.add(new Move(location, getMove(myID, site, location, gameMap)));
                    }
                }
            }
            Networking.sendFrame(moves);
        }
    }
    private static Direction getMove(int myID, Site site, Location location, GameMap map){
    	boolean borderUnit = false;
    	ArrayList<Direction> attackMoves = new ArrayList<Direction>();
    	Site nextSite;
    	Location neighbor;
    	//Checks if a bot is a border unit
    	for(Direction d : Direction.CARDINALS){
    		neighbor = map.getLocation(location, d);
    		nextSite = neighbor.getSite();
    		if(myID != nextSite.owner)
    			borderUnit = true;
    	}
    	if(borderUnit){
    		//moves where a bot can take a tile then take the tile with the highest production strength ratio
        	for(Direction d : Direction.CARDINALS){
        		neighbor = map.getLocation(location, d);
        		nextSite = neighbor.getSite();
        		if(site.strength > nextSite.strength && myID != nextSite.owner)
        			attackMoves.add(d);
        	}
        	if(!attackMoves.isEmpty())
        		return attack(site, location, map, attackMoves);
    	}else if(site.strength > 10 * site.production || site.strength > 40){
        	//Bots not near the border will go to the nearest border
        	return nearestBorder(myID, location, map);
    	}
    	return Direction.STILL;
    }
    private static int nearestDistance(int myID, Site site, Location location, GameMap map){
    	int ctr;
    	int maxDistance = Math.min(map.height, map.width) / 2;
    	for(Direction d : Direction.CARDINALS){
    		ctr = 0;
    		Location loc = location;
        	Site s = map.getSite(loc, d);
    		while(s.owner == myID && ctr < maxDistance){
    			ctr++;
    			loc = map.getLocation(loc, d);
    			s = map.getSite(loc);
    		}
    		if(ctr < maxDistance){
    			maxDistance = ctr;
    		}
    	}
    	return maxDistance;
    }
    private static Direction nearestBorder(int myID, Location location, GameMap map){
    	int ctr;
    	Direction dir = Direction.NORTH;
    	int maxDistance = Math.min(map.height, map.width) / 2;
    	for(Direction d : Direction.CARDINALS){
    		ctr = 0;
    		Location loc = location;
        	Site s = map.getSite(loc, d);
    		while(s.owner == myID && ctr < maxDistance){
    			ctr++;
    			loc = map.getLocation(loc, d);
    			s = map.getSite(loc);
    		}
    		if(ctr < maxDistance){
    			maxDistance = ctr;
    			dir = d;
    		}
    	}
    	return dir;
    }
    private static Direction attack(Site site, Location location, GameMap map, ArrayList<Direction> favorableMoves){
    	int best = 0;
    	for(int i = 0; i < favorableMoves.size(); i++){
			if(i == 0)
    			best = i;
			if(worth(map.getSite(location, favorableMoves.get(best))) < worth(map.getSite(location, favorableMoves.get(i))))
				best = i;
    	}
    	return favorableMoves.get(best);
    }
    private static double worth(Site site){
    	if(site.strength == 0)
    		return site.production;
    	return site.production / (double)site.strength;
    }
    private static Direction getRandomDirection(Direction[] arr){
    	Random r = new Random();
    	return arr[r.nextInt(arr.length)];
    }
}
