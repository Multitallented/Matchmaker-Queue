import org.json.JSONArray;

import java.text.NumberFormat;
import java.util.*;

/**
 * Created by multitallented on 3/8/17.
 */
public class OverwatchQueue {
    public static void main(String[] args) {
        new OverwatchQueue();
    }

    private JSONArray eloGroups = new JSONArray("[" +
            "[1000, 1500, 1300, 800]," +
            "[800]," +
            "[700]," +
            "[1400, 1500, 1300]," +
            "[900]," +
            "[800, 750]," +
            "[1009, 1200]," +
            "[700,1400]," +
            "[1100]," +
            "[1500, 1400, 1300, 1450, 1200]," +
            "[1000]," +
            "[1200, 800]," +
            "[1350]," +
            "[720]," +
            "[850]," +
            "[1200]," +
            "[1150]," +
            "[1493,1307]" +
        "]");
    private static int countMatches = 0;

    public OverwatchQueue() {

        ArrayList<EloGroup> processedEloGroups = new ArrayList<>();
        for (Object eloGroup : eloGroups) {
            processedEloGroups.add(new EloGroup((JSONArray) eloGroup));
        }

        Match match = findBestMatch(processedEloGroups);
        if (match == null) {
            System.out.println("No matches found");
            return;
        }
        System.out.println("Best Match: " + match.getAbsRatingDifference());
        String msg = "";
        for (EloGroup currentGroup : match.getTeam(1)) {
            msg += currentGroup.toString();
        }

        System.out.println("Team 1 (" + match.getTeamAverage(1) + "): " + msg);

        msg = "";
        for (EloGroup currentGroup : match.getTeam(2)) {
            msg += currentGroup.toString();
        }
        System.out.println("Team 2 (" + match.getTeamAverage(2) + "): " + msg);
        System.out.println("Matches attempted: " + NumberFormat.getNumberInstance().format(countMatches));
    }

    private Match findBestMatch(ArrayList<EloGroup> processedEloGroups) {
        Match bestMatch = null;
        double closestRating = 999999;

        ArrayList<ArrayList<EloGroup>> allCombinations = generateCombinations(processedEloGroups);

        outer: for (ArrayList<EloGroup> currentList : allCombinations) {
            inner: for (ArrayList<EloGroup> currentList2 : allCombinations) {
                if (currentList.equals(currentList2)) {
                    continue;
                }
                for (EloGroup group1 : currentList) {
                    if (currentList2.contains(group1)) {
                        continue inner;
                    }
                }
                Match currentMatch = new Match(currentList, currentList2);
                countMatches++;
                if (currentMatch.getAbsRatingDifference() < closestRating) {
                    if (currentMatch.getAbsRatingDifference() == 0) {
                        return currentMatch;
                    }
                    bestMatch = currentMatch;
                    closestRating = currentMatch.getAbsRatingDifference();
                }
            }
        }

        return bestMatch;
    }

    private ArrayList<ArrayList<EloGroup>> generateCombinations(ArrayList<EloGroup> processedEloGroups) {

        ArrayList<ArrayList<EloGroup>> subsets = new ArrayList<>();

        for (int l=6; l>0; l--) {
            int[] s = new int[l];                  // here we'll keep indices
            // pointing to elements in input array

            if (l <= processedEloGroups.size()) {
                // first index sequence: 0, 1, 2, ...
                for (int i = 0; (s[i] = i) < l - 1; i++);
                {
                    ArrayList<EloGroup> tempList = getSubset(processedEloGroups, s);
                    if (tempList != null) subsets.add(tempList);
                }
                for(;;) {
                    int i;
                    // find position of item that can be incremented
                    for (i = l - 1; i >= 0 && s[i] == processedEloGroups.size() - l - 1 + i; i--);
                    if (i < 0) {
                        break;
                    } else {
                        s[i]++;                    // increment this item
                        for (++i; i < l; i++) {    // fill up remaining items
                            s[i] = s[i - 1] + 1;
                        }
                        ArrayList<EloGroup> tempList = getSubset(processedEloGroups, s);
                        if (tempList != null && !subsets.contains(tempList)) subsets.add(tempList);
                    }
                }
            }
        }
        return subsets;
    }
    private ArrayList<EloGroup> getSubset(ArrayList<EloGroup> processedEloGroups, int[] subset) {
        // generate actual subset by index sequence
        ArrayList<EloGroup> result = new ArrayList<>();
        int currentSize = 0;
        for (int i = 0; i < subset.length; i++) {
            EloGroup currentGroup = processedEloGroups.get(subset[i]);
            if (currentSize + currentGroup.getSize() > 6) {
                continue;
            }
            currentSize += currentGroup.getSize();
            result.add(processedEloGroups.get(subset[i]));
        }
        if (currentSize != 6) return null;
        return result;
    }

    private class EloGroup {
        private final JSONArray groupArray;
        private double average = -1;
        private double total = -1;

        public EloGroup(JSONArray groupArray) {
            this.groupArray = groupArray;
        }

        public int getSize() {
            return groupArray.length();
        }

        public double getAverage() {
            if (average == -1) {
                int runningTotal = 0;
                for (Object currentRating : groupArray) {
                    runningTotal += (int) currentRating;
                }
                average = ((double) runningTotal) / ((double) groupArray.length());
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
        public JSONArray getGroupArray() {
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
