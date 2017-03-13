import org.json.JSONArray;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by multitallented on 3/13/17.
 */
public class WoT {

    public static void main(String[] args) { new WoT(); }

    public WoT() {
        randomizeGroups();
        long timeStart = System.currentTimeMillis();


        System.out.println("Time: " + NumberFormat.getNumberInstance().format(System.currentTimeMillis() - timeStart));
//        System.out.println("Diff: " + match.getAbsRatingDifference());
        System.out.println("Matches: " + NumberFormat.getNumberInstance().format(countMatches));
        System.out.println("Teams: " + NumberFormat.getNumberInstance().format(countTeams));

    }

    private Match findBestMatch(ArrayList<ArrayList<EloGroup>> allCombinations, ArrayList<EloGroup> currentGroup) {
        Match bestMatch = null;
        double closestRating = 999999;


        outer: for (ArrayList<EloGroup> currentList : allCombinations) {
            if (currentList.equals(currentGroup)) {
                continue;
            }
            for (EloGroup group1 : currentList) {
                if (currentGroup.contains(group1)) {
                    continue outer;
                }
            }
            Match currentMatch = new Match(currentList, currentGroup);
            countMatches++;
            if (currentMatch.getAbsRatingDifference() < closestRating) {
                if (currentMatch.getAbsRatingDifference() < tolerance) {
                    return currentMatch;
                }
                bestMatch = currentMatch;
                closestRating = currentMatch.getAbsRatingDifference();
            }
        }

        return bestMatch;
    }

    private void randomizeGroups() {
        int randLength = 25;
        System.out.println(randLength);
        for (int i=0;i<randLength; i++) {
            double rand = Math.random();
            int randSize = 1;
            if (rand < 0.1) {
                randSize = 3;
            } else if (rand < 0.25) {
                randSize = 2;
            }

            ArrayList<Player> tempList = new ArrayList<>();
            for (int j=0;j<randSize;j++) {
                int rating = (int) (Math.random() * 7000 + 1000);
                int tier = getTier(rating);
                TankType type = getTankType(tier);
                Player player = new Player(rating, type, tier);
                tempList.add(player);
            }
            eloGroups.add(new EloGroup(tempList));
        }
    }

    private int getTier(int rating) {
        double rand = Math.random();
        if (rand < 0.1 && rating < 3001) {
            return 1;
        } else if (rand < 0.2 && rating < 3501) {
            return 2;
        } else if (rand < 0.3 && rating < 4501) {
            return 3;
        } else if (rand < 0.4 && rating < 5501) {
            return 4;
        } else if (rand < 0.5 && rating < 7001) {
            return 5;
        } else if (rand < 0.6) {
            return 6;
        } else if (rand < 0.7) {
            return 7;
        } else if (rand < 0.8) {
            return 8;
        } else if (rand < 0.9) {
            return 9;
        } else {
            return 10;
        }
    }

    private TankType getTankType(int tier) {
        double rand = Math.random();
        if (rand < 0.2 && tier > 3) {
            return TankType.HEAVY;
        } else if (rand < 0.4 && tier > 1) {
            return TankType.ARTILLERY;
        } else if (rand < 0.6 && tier > 1) {
            return TankType.TANK_DESTROYER;
        } else if (rand < 0.8 && tier < 9) {
            return TankType.LIGHT;
        } else {
            return TankType.MEDIUM;
        }
    }

    private enum TankType {
        LIGHT,
        MEDIUM,
        HEAVY,
        TANK_DESTROYER,
        ARTILLERY
    }

    private static ArrayList<EloGroup> eloGroups = new ArrayList<>();
    private static long countTeams = 0;
    private static long countMatches = 0;
    private int tolerance = 10;

    private class Player {
        private final int rating;
        private final TankType type;
        private final int tier;

        public Player(int rating, TankType type, int tier) {
            this.rating = rating;
            this.type = type;
            this.tier = tier;
        }
        public int getRating() {
            return rating;
        }
        public TankType getType() {
            return type;
        }
        public int getTier() {
            return tier;
        }
    }

    private class EloGroup {
        private final ArrayList<Player> groupArray;
        private double average = -1;
        private double total = -1;

        public EloGroup(ArrayList<Player> groupArray) {
            this.groupArray = groupArray;
        }

        public int getSize() {
            return groupArray.size();
        }

        public double getAverage() {
            if (average == -1) {
                int runningTotal = 0;
                for (Player currentRating : groupArray) {
                    runningTotal += currentRating.getRating();
                }
                average = ((double) runningTotal) / ((double) groupArray.size());
            }
            return average;
        }
        public double getTotal() {
            if (total == -1) {
                total = 0;
                for (Object currentRating : groupArray) {
                    total += (int) currentRating;
                }
            }
            return total;
        }
        public ArrayList<Player> getGroupArray() {
            return groupArray;
        }

        @Override
        public String toString() {
            String msg = "";
            for (Object currentRating : groupArray) {
                msg += "" + (int) currentRating + " ";
            }
            return msg;
        }
    }

    private class Match {
        private ArrayList<EloGroup> team1;
        private ArrayList<EloGroup> team2;
        private double absRatingDifference = -1;
        public boolean hasValidMakeup() {
            int[] types1 = countTypes(team1);
            if (types1[4] > 4) {
                return false;
            }
            int[] types2 = countTypes(team2);
            if (types2[4] > 4) {
                return false;
            }
            for (int i=0; i<5;i++) {
                if (Math.abs(types1[i] - types2[i])>1) {
                    return false;
                }
            }

            return true;
        }

        private int[] countTypes(ArrayList<EloGroup> groups) {
            int[] types = new int[5];

            for (EloGroup group : groups) {
                for (Player player : group.getGroupArray()) {
                    if (player.getType() == TankType.TANK_DESTROYER) {
                        types[3]++;
                    } else if (player.getType() == TankType.ARTILLERY) {
                        types[4]++;
                    } else if (player.getType() == TankType.LIGHT) {
                        types[0]++;
                    } else if (player.getType() == TankType.MEDIUM) {
                        types[1]++;
                    } else {
                        types[2]++;
                    }
                }
            }
            return types;
        }

        public Match(ArrayList<EloGroup> team1, ArrayList<EloGroup> team2) {
            this.team1 = team1;
            this.team2 = team2;
        }
        public double getAbsRatingDifference() {
            if (absRatingDifference == -1) {
                double team1Avg = getTeamAverage(1);
                double team2Avg = getTeamAverage(2);

                this.absRatingDifference = Math.abs(team1Avg - team2Avg);
            }
            return absRatingDifference;
        }
        public void addToTeam(EloGroup currentGroup, int teamNumber) {
            if (teamNumber == 1) {
                team1.add(currentGroup);
            } else {
                team2.add(currentGroup);
            }
        }
        public void setTeam(ArrayList<EloGroup> newTeam, int teamNumber) {
            if (teamNumber == 1) {
                team1 = newTeam;
            } else {
                team2 = newTeam;
            }
        }
        public double getTeamAverage(int teamNumber) {
            ArrayList<EloGroup> currentTeam = teamNumber == 1 ? team1 : team2;
            double average = 0;

            for (EloGroup currentGroup : currentTeam) {
                average += currentGroup.getTotal();
            }
            return average / 6;
        }
        public ArrayList<EloGroup> getTeam(int teamNumber) {
            if (teamNumber == 1) {
                return team1;
            } else {
                return team2;
            }
        }
    }
}
