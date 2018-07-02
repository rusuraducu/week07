import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class GetLinks {

    private static Set<String> globalSetWithLinks = new LinkedHashSet<>();

    public static void main(String[] args) {
        GetLinks GetLinks = new GetLinks();
        Set<String> urlList = new HashSet<>();

        //The program gets the initial url.
        urlList.add("https://en.wikipedia.org/wiki/FGE");  //For this url the program found 23526 links.
        GetLinks.writeLinksFeedBack(GetLinks, urlList);

    }

    //Feedback
    private void writeLinksFeedBack(GetLinks GetLinks, Set<String> urlList) {
        if (!isValidLink(urlList)) {
            System.out.println("The url is not valid.");
        } else {
            int noLinks = GetLinks.writeAllTheLinksInTheTextFile(urlList);
            System.out.println(noLinks + " links have been written to the text file.");
        }
    }

    // Each url will be accessed and the program will get all the links from each url and
    // will add them in a Temporary HashSet. After that, before the end of the method, they will be moved in the
    // Global Set With Links. At each recall, the method it is going to get as parameter
    // the Set "globalSetWithLinks".

    private boolean getLinksUpToTheThirdGeneration(Set<String> urlList, int recursion) {
        Set<String> temp = new HashSet<>();
        for (String url : urlList) {
            temp.addAll(getAllTheLinksFromPage(getContent(formatURL(url))));
        }
        addTheLinksInTheGlobalSetWithLinks(temp);
        recursion = recursion - 1;
        if (recursion > 0) {
            getLinksUpToTheThirdGeneration(globalSetWithLinks, recursion);
        }
        return true;
    }

    //Get all the links from a single page.
    private Set<String> getAllTheLinksFromPage(String content) {
        Set<String> getLinks = new HashSet<>();
        int i = 0;
        while (true) {
            int found = content.indexOf("href=\"/wiki", i);
            if (found == -1) {
                break;
            }
            int start = found + 6;
            int end = content.indexOf("\"", start);
            String urlLink = content.substring(start, end);
            getLinks.add(urlLink + "\n");
            i = end + 1;
        }
        return getLinks;
    }

    //Get content from a single page.
    private String getContent(String url) {

        StringBuilder Builder = new StringBuilder();
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(connection(url).getInputStream()))) {
            String Line = new String();
            while ((Line = bf.readLine()) != null) {
                if (isArticle(Line)) {
                    Builder.append(Line + "\n");
                }
            }
        } catch (Exception ex) {

        }
        return Builder.toString();
    }

    //Write all the links from globalSetWithLinks in a text file.
    private int writeAllTheLinksInTheTextFile(Set<String> urlList) {
        getLinksUpToTheThirdGeneration(urlList, 3); // recursion "3" means to get the links up to the third "generation".
        int i = 0;
        if (theFileExists()) {
            clearFile(); //Empty it!
        }
        for (String g : globalSetWithLinks) {
            i++;
            String url = i + ". " + formatURL(g);
            writeLink(url);
        }
        return i;
    }

    //Check if the initial link is valid.
    private static boolean isValidLink(Set<String> urls) {
        for (String u : urls) {
            if (u.startsWith("https://en.wikipedia.org/") == true || u.startsWith("/wiki") == true) {
                return true;
            }
        }
        return false;
    }



    //Create a connection.
    private HttpURLConnection connection(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException ex) {
            ex.getStackTrace();
        }
        return conn;
    }

    //The links which go to articles start with /wiki and don't contain ":"
    private boolean isArticle(String url) {
        if (url.contains("/wiki/Main_Page")) {
            return false;
        }
        for (char c : url.toCharArray()) {
            if (c == ':') {
                return false;
            }
        }
        return true;
    }

    //Move the links to the Global Set With Links and empty temporary set.
    private void addTheLinksInTheGlobalSetWithLinks(Set<String> temp) {
        globalSetWithLinks.addAll(temp);
        temp.clear();
    }

    //Next method is going to be used to write a link in the text file.
    private String writeLink(String url) {
        try (BufferedWriter bf = new BufferedWriter(new FileWriter("D:\\globalSetWithLinks.txt", true))) {
            bf.write(url);
            bf.newLine();
        } catch (IOException ex) {
            ex.getStackTrace();
        }
        return url;
    }

    //The links obtained will be formatted. "/wiki/Article" => "https://en.wikipedia.org/Article"
    private String formatURL(String url) {
        if (url.startsWith("https://en.wikipedia.org") != true) {
            url = "https://en.wikipedia.org" + url;
        }
        return url;
    }

    //Check if the text file exists.
    private boolean theFileExists() {
        BufferedReader bfr = null;
        try {
            bfr = new BufferedReader(new FileReader("D:\\globalSetWithLinks.txt"));
            bfr.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    //If the text file exists it is going to be emptied each time when the program is running.
    //Only if the file is empty it is going to be filled with links.

    private void clearFile() {
        try {
            FileWriter fw = new FileWriter("D:\\globalSetWithLinks.txt");
            fw.write("");
            fw.close();
        } catch (IOException io) {
           io.getStackTrace();
        }
    }
}
