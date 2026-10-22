package uce.desarrolloweb.ProgramacionWeb001.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests that drive the real app in a real browser.
 * Videos → target/videos/
 * Screenshots → target/screenshots/
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppE2ETest {

    @LocalServerPort
    private int port;

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    private static final int DEFAULT_TIMEOUT = 15000;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    // ============================================================
    // SETUP / TEARDOWN
    // ============================================================
    @BeforeAll
    static void launchBrowser() throws Exception {
        Files.createDirectories(Paths.get("target/videos"));
        Files.createDirectories(Paths.get("target/screenshots"));

        boolean headless = !"false".equalsIgnoreCase(
                System.getProperty("playwright.headless", "true"));

        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("target/videos"))
                        .setRecordVideoSize(1280, 720)
                        .setViewportSize(1280, 720));
        context.setDefaultTimeout(DEFAULT_TIMEOUT);
        context.setDefaultNavigationTimeout(DEFAULT_TIMEOUT);
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();   // saves the video file
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private void screenshot(String name) {
        try {
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("target/screenshots", name + ".png"))
                    .setFullPage(true));
        } catch (Exception ignored) { }
    }

    private void waitVisible(String selector) {
        page.waitForSelector(selector,
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE));
    }

    /**
     * Click a Pokémon card by name.
     * The card markup has data attributes we can query.
     */
    private void clickPokemonCard(String name) {
        page.locator(String.format(".pokemon-card:has-text('%s')", name))
                .first()
                .click();
    }

    // ============================================================
    // PAGE 1 — HOME
    // ============================================================

    @Test
    @Order(1)
    void homePageLoadsAndShowsBooks() {
        page.navigate(baseUrl() + "/");

        assertThat(page.locator("h1")).containsText("Featured Books");

        page.waitForSelector(".prestamo-card",
                new Page.WaitForSelectorOptions().setTimeout(10000));
        int cards = page.locator(".prestamo-card").count();
        assertTrue(cards > 0, "Home page should show at least one book, found " + cards);

        assertThat(page.locator("a[href='/pokemon']")).isVisible();

        screenshot("01-home-page");
    }

    @Test
    @Order(2)
    void bookCardViewModalOpens() {
        page.navigate(baseUrl() + "/");
        page.waitForSelector(".prestamo-card");

        page.locator(".btn-view").first().click();

        waitVisible("#viewModal.show");
        assertThat(page.locator("#viewTitle")).not().isEmpty();

        screenshot("02-book-view-modal");
    }

    @Test
    @Order(3)
    void searchBarFiltersBooks() {
        page.navigate(baseUrl() + "/");
        page.waitForSelector(".prestamo-card");

        int originalCount = page.locator(".prestamo-card").count();
        assertTrue(originalCount > 0, "Need at least one book to test search");

        page.locator("#searchInput").fill("tesoro");

        page.waitForTimeout(600);
        int visibleCount = page.locator(".book-item:not([style*='display: none'])").count();
        assertTrue(visibleCount > 0 && visibleCount <= originalCount,
                "Search should filter the grid (was " + originalCount + ", now " + visibleCount + ")");

        screenshot("03-search-filtered");
    }

    // ============================================================
    // PAGE 2 — POKÉMON
    // ============================================================

    @Test
    @Order(4)
    void navigateToPokemonPage() {
        page.navigate(baseUrl() + "/");
        page.locator("a[href='/pokemon']").first().click();
        page.waitForURL("**/pokemon");

        assertThat(page.locator("h1")).containsText("Pokémon Explorer");
        assertThat(page.locator("a[href='/pokemon']")).isVisible();

        // Browse list should eventually load
        page.waitForSelector(".pokemon-card",
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT));

        screenshot("04-pokemon-page");
    }

    @Test
    @Order(5)
    void pokemonPickerOpensOnCardClick() {
        page.navigate(baseUrl() + "/pokemon");
        page.waitForSelector(".pokemon-card");

        clickPokemonCard("bulbasaur");

        waitVisible("#pickerModal.show");

        assertThat(page.locator("#pickerName")).containsText("bulbasaur");
        assertThat(page.locator(".picker-choice[data-endpoint='species']")).isVisible();
        assertThat(page.locator(".picker-choice[data-endpoint='evolution']")).isVisible();
        assertThat(page.locator(".picker-choice[data-endpoint='stats']")).isVisible();

        screenshot("05-picker-modal");
    }

    @Test
    @Order(6)
    void speciesEndpointOpensItsOwnModal() {
        page.navigate(baseUrl() + "/pokemon");
        page.waitForSelector(".pokemon-card");

        clickPokemonCard("bulbasaur");
        waitVisible("#pickerModal.show");

        page.locator(".picker-choice[data-endpoint='species']").click();

        // Wait for the picker to hide AND species modal to appear
        waitVisible("#speciesModal.show");
        page.waitForSelector("#speciesModalBody .pokedex-grid",
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT));

        // Header shows the endpoint being used
        assertThat(page.locator("#speciesEndpoint")).containsText("/pokemon-species/bulbasaur");

        // Data rendered
        int items = page.locator("#speciesModalBody .pokedex-item").count();
        assertTrue(items >= 5, "Species modal should show species facts (found " + items + ")");

        screenshot("06-species-modal");
    }

    @Test
    @Order(7)
    void evolutionEndpointOpensItsOwnModal() {
        page.navigate(baseUrl() + "/pokemon");
        page.waitForSelector(".pokemon-card");

        clickPokemonCard("bulbasaur");
        waitVisible("#pickerModal.show");

        page.locator(".picker-choice[data-endpoint='evolution']").click();

        waitVisible("#evolutionModal.show");
        page.waitForSelector("#evolutionModalBody .evolution-card",
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT));

        assertThat(page.locator("#evolutionEndpoint")).containsText("/evolution-chain/");

        int stages = page.locator("#evolutionModalBody .evolution-card").count();
        assertTrue(stages >= 2, "Bulbasaur's chain should have ≥ 2 stages (found " + stages + ")");

        assertThat(page.locator("#evolutionModalBody")).containsText("ivysaur");

        screenshot("07-evolution-modal");
    }

    @Test
    @Order(8)
    void statsEndpointOpensItsOwnModal() {
        page.navigate(baseUrl() + "/pokemon");
        page.waitForSelector(".pokemon-card");

        clickPokemonCard("bulbasaur");
        waitVisible("#pickerModal.show");

        page.locator(".picker-choice[data-endpoint='stats']").click();

        waitVisible("#statsModal.show");
        page.waitForSelector("#statsModalBody .stat-bar-row",
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT));

        assertThat(page.locator("#statsEndpoint")).containsText("/pokemon/bulbasaur");

        int rows = page.locator("#statsModalBody .stat-bar-row").count();
        assertTrue(rows >= 6, "Battle stats should show 6 stat rows (found " + rows + ")");

        screenshot("08-stats-modal");
    }

    @Test
    @Order(9)
    void backToPickerFromEndpoint() {
        page.navigate(baseUrl() + "/pokemon");
        page.waitForSelector(".pokemon-card");

        clickPokemonCard("bulbasaur");
        waitVisible("#pickerModal.show");

        // Open species, then go back
        page.locator(".picker-choice[data-endpoint='species']").click();
        waitVisible("#speciesModal.show");

        page.locator(".back-to-picker[data-from='species']").click();

        // Picker reopens
        waitVisible("#pickerModal.show");

        screenshot("09-back-to-picker");
    }

    @Test
    @Order(10)
    void evolutionStageClickOpensNewPicker() {
        page.navigate(baseUrl() + "/pokemon");
        page.waitForSelector(".pokemon-card");

        clickPokemonCard("bulbasaur");
        waitVisible("#pickerModal.show");

        // Open evolution
        page.locator(".picker-choice[data-endpoint='evolution']").click();
        waitVisible("#evolutionModal.show");
        page.waitForSelector("#evolutionModalBody .evolution-card",
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT));

        // Click the ivysaur stage (not the current pokemon)
        page.locator("#evolutionModalBody .evolution-card[data-name='ivysaur']").first().click();

        // A new picker for ivysaur should open
        waitVisible("#pickerModal.show");
        assertThat(page.locator("#pickerName")).containsText("ivysaur");

        screenshot("10-picker-from-evolution");
    }

    @Test
    @Order(11)
    void searchDelayShowsSuggestions() {
        page.navigate(baseUrl() + "/pokemon");

        // Type in the search bar — nothing should appear immediately
        page.locator("#pokemonSearch").fill("pikachu");

        // Immediately after typing: no dropdown yet (4-second delay)
        assertThat(page.locator("#suggestions")).not().hasClass(
        java.util.regex.Pattern.compile(".*open.*"));

        // Wait 4+ seconds for the debounce to fire
        page.waitForTimeout(4500);

        // Suggestions should now be visible
        page.waitForSelector("#suggestions.open",
                new Page.WaitForSelectorOptions().setTimeout(5000));

        int suggestions = page.locator("#suggestions .suggestion-item").count();
        assertTrue(suggestions > 0, "Should show suggestions after 4s (found " + suggestions + ")");

        assertThat(page.locator("#suggestions")).containsText("pikachu");

        screenshot("11-search-suggestions");
    }
}