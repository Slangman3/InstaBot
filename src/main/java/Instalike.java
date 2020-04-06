import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Map.entry;

class Instalike {

    private final Map<String, String> xpaths = Map.ofEntries(
            entry("Log_in", "//*[contains(text(), 'Log In')]"),
            entry("Subscribe", "//*[text() = 'Follow' || text() =  'Follow Back']"),
            entry("Subscribed", "//*[text() = 'Following']"),
            entry("First_photo", "//*[@id='react-root']/section/main/div/div[4]/article/div[1]/div/div[1]/div[1]"),
            entry("Like_button", "/html/body/div[4]/div[2]/div/article/div[2]/section[1]/span[1]/button"),
            entry("Next_button_first", "//*[@id='react-root']/html/body/div[3]/div/div[1]/div/div/a"),
            entry("Next_button_default", "//*[@id='react-root']/html/body/div[3]/div/div[1]/div/div/a[2]"),
            entry("Next_button_last", "//*[@id='react-root']/html/body/div[3]/div/div[1]/div/div/a")
    );
    private final String login;
    private final String password;
    private final String target;
    private ChromeDriver browser;
    private int baseDelay;
    private Logger logger;

    Instalike(String login, String password,
              String target) {
        this(login, password, target, 3);
    }

    Instalike(String login, String password,
              String target, int baseDelay) {
        this.login = login;
        this.password = password;
        this.target = target;
        this.baseDelay = baseDelay;
        logger = Logger.getLogger(Instalike.class.getName());
    }

    void start() {
        log("InstaBot начал свою работу!");
        browser = new ChromeDriver();
        browser.manage().window().maximize();
        login();
        openTarget();
        addFolowers(12);
//        subscribe();
        findFirst();
        likeCurrent();
        getNext();
        likeCurrent();
        closeLink();
//        end();
    }

    private void login() {
        browser.get("https://www.instagram.com/accounts/login/");
        waitASecond();
        browser.findElement(new By.ByName("username")).sendKeys(login);
        waitASecond();
        browser.findElement(new By.ByName("password")).sendKeys(password);
        browser.findElement(new By.ByXPath(xpaths.get("Log_in"))).click();
        waitASecond();
        log(String.format("Залогинились, как %s", login));
    }

    private void openTarget() {
        browser.get(String.format("https://www.instagram.com/%s", target));
        log(String.format("Зашли на страничку к %s", target));
        waitASecond();
        browser.findElement(By.xpath(String.format("//*[@href='/%s/followers/']", target))).click();
        log(String.format("Открыли список фоловеров %s", target));
        waitASecond();
    }

    public void clickLink() {
        ArrayList<String> tabs = new ArrayList<String>(browser.getWindowHandles());
        browser.switchTo().window(tabs.get(1));
    }

    public void closeLink() {
        ArrayList<String> tabs = new ArrayList<String>(browser.getWindowHandles());
        browser.close();
        browser.switchTo().window(tabs.get(0));
    }

    private boolean addFolowers(int n) {
        for (int i = 1; i < n; i++) {
            Actions newTab = new Actions(browser);
            newTab.keyDown(Keys.CONTROL).click(browser.findElement(By.xpath(String.format("/html/body/div[4]/div/div[2]/ul/div/li[%s]/div/div[1]/div[1]", i)))).keyUp(Keys.CONTROL).build().perform();
            clickLink();
            waitASecond();
            String userName = browser.findElement(By.xpath("//*[@id='react-root']/section/main/div/header/section/div[1]/h1")).getText();
            log(String.format("Подписываемся на %s.", userName));
            try {
                browser.findElement(By.xpath("//button[text() = 'Follow']")).click();
                waitASecond();
                return true;
            } catch (NoSuchElementException ex) {
                warn("Уже был подписан!");
                return false;
            }
        }
        return true;
    }

    private boolean subscribe() {
        browser.get(String.format("https://www.instagram.com/%s", target));
        waitASecond();
        log(String.format("Подписываемся на %s.", target));
        try {
            browser.findElement(new By.ByXPath(xpaths.get("Subscribe"))).click();
            waitASecond();
            return true;
        } catch (NoSuchElementException ex) {
            warn("Уже был подписан!");
            return false;
        }
    }

    private boolean findFirst() {
        String userName = browser.findElement(By.xpath("//*[@id='react-root']/section/main/div/header/section/div[1]/h1")).getText();
        log(String.format("Ставим лойсы для %s.", userName));
        waitASecond();
        try {
            browser.findElement(new By.ByXPath(xpaths.get("First_photo"))).isDisplayed();
            browser.findElement(new By.ByXPath(xpaths.get("First_photo"))).click();
            waitASecond();
            return true;
        } catch (NoSuchElementException ex) {
            warn("Не могу найти первое фото!");
            return false;
        }
    }

    /**
     * Лайк-кодим открытую фотографию.
     */
    private void likeCurrent() {
        var likeButton = browser.findElement(new By.ByXPath(xpaths.get("Like_button")));
        try {
            var param = browser.findElement(By.xpath("//*[@aria-label='Unlike']"));
            log("Тут лойс уже стоит!");
        } catch (NoSuchElementException ex) {
            likeButton.click();
            log("Лойс поставлен!");
            waitASecond();
        }
    }

    /**
     * Открываем следующую фототграфию.
     *
     * @return True, если удалось открыть, false в противном случае.
     */
    private boolean getNext() {
        try {
            browser.findElement(new By.ByClassName("coreSpriteRightPaginationArrow")).click();
            waitASecond();
            return true;
        } catch (NoSuchElementException e) {
            warn("Не могу открыть следующее фото. Кажется, закончились.");
            return false;
        }
    }

    /**
     * Заканчиваем работу браузера.
     */
    private void end() {
        browser.close();
        log("Сегодня поработал!");
    }

    /**
     * Ждём секунду. На самом деле от трёх до пяти, случайным образом решается.
     */
    private void waitASecond() {
        try {
            Thread.sleep((baseDelay + ThreadLocalRandom.current().nextInt(1, 3)) * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Логируем сообщение.
     *
     * @param message Текст сообщения.
     */
    private void log(String message) {
        logger.log(Level.INFO, String.format("Instalike: %s", message));
    }

    /**
     * Логируем предупреждение.
     *
     * @param message Текст предупреждения.
     */
    private void warn(String message) {
        logger.log(Level.WARNING, String.format("Instalike: %s", message));
    }
}