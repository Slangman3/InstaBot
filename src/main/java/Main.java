import java.io.IOException;

public class Main {

    private static CredentialProperties credentials;

    public static void main(String[] args) {
        setup();
        System.out.println("Запуск бота...");
        var bot = new Instalike(
                credentials.getLogin(),
                credentials.getPassword(),
                "olumov6"
        );
        bot.start();
    }

    private static void setup() {

        System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT [%4$s]: %5$s%6$s%n"
        );

        try {
            credentials = new CredentialProperties();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.setProperty(
                "webdriver.chrome.driver",
                "C:\\Users\\ilyavoronovich\\IdeaProjects\\InstaBot\\src\\main\\driver\\chromedriver.exe"
        );
    }
}