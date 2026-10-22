The program utilizes an MVC structure, uses Java 17, and requires UTF-8 encoding; otherwise, some characters may appear in strange ways. It is built with the PostgreSQL database.

The program is divided into the following structure:

ProgramacionWeb001/
├── .github/workflows/playwright-demo.yml       ← CI/CD workflow
├── docs/demo-videos/*.webm                     ← Committed demo videos (optional but recommended)
├── pom.xml                                     ← Maven dependencies
├── README.md                                   ← Project overview
├── .gitignore                                  ← Excludes target/, .idea/, etc.
├── src/main/
│   ├── java/uce/desarrolloweb/ProgramacionWeb001/
│   │   ├── ProgramacionWeb001Application.java  ← Spring Boot entry point
│   │   ├── Controller/
│   │   │   ├── LibroController.java            ← Page 1: books, 5 REST endpoints
│   │   │   └── PokemonController.java          ← Page 2: view resolver only
│   │   ├── Model/Libro.java                    ← JPA entity
│   │   ├── Repository/LibroRepository.java     ← DB queries
│   │   └── Service/LibroService.java           ← Business logic (shuffle)
│   └── resources/
│       ├── application.properties              ← Base config (sets dev profile)
│       ├── application-dev.properties          ← H2 in-memory config
│       ├── application-prod.properties         ← PostgreSQL config
│       ├── data.sql                            ← 86 books preloaded
│       └── templates/
│           ├── index.html                      ← Books page (REAL-TIME search)
│           └── pokemon.html                    ← Pokémon page (4s DELAY search)
└── src/test/java/uce/desarrolloweb/ProgramacionWeb001/e2e/
    └── AppE2ETest.java                         ← 11 Playwright tests

The dependencies for this project were added using Spring Initializer, with Thymeleaf, Spring JPA for persisting data in SQL, Spring Web for building web applications, and the PostgreSQL Driver for the connection between the project and the PostgreSQL database. Three other dependencies were installed: H2 for an in-memory DB for development, Playwright for E2E browser automation, and exec-maven-plugin to install Chromium binaries.

The application.properties file in the base code is used for Thymeleaf, encoding, logging, and the two profiles: prod for PostgreSQL and dev for H2 in-memory.

The application-dev.properties file is used so the project does not depend on external servers like PostgreSQL. It is used for the demo version, so everyone who clones the repo gets an H2 database created fresh on every startup.

The application-prod.properties file is used for the configuration of PostgreSQL.

The project has a data.sql file with 86 books from another project. Six of these books are randomly chosen so they can appear on screen every time it runs.

In the Model package, the main entity is Libro, which has titulo, author, editorial, anioPublicacion, and categoria as Strings and disponible as a Boolean.

The Repository package is used to find Libro and filter searches by titulo and categoria.

In the Service package, there is the business logic such as create, modify, list, and delete. The ones connected with the API return JSON, not HTML.

In the Controller package, there are two files: LibroController and PokemonController. These two are the bridge between the service and the templates; they define how to display the page.

Now, in the templates, there are two HTML files. First, let's concentrate on index.html.

Index Features:

Real-time Search: searchInput.addEventListener('input', ...)

Autocomplete dropdown .suggestions div + renderSuggestions()

View Modal: Fetch by id openViewModal(id)

Edit Modal: openEditModal(id)

Pokemon Features:

4-second search delay: const SEARCH_DELAY_MS = 4000;

3 endpoint choices: document.querySelectorAll('.picker-choice').forEach(btn =>

How the debounce works:

User types p → timer starts (4s)

User types i at t=1s → cancel old timer, start new 4s

User types k at t=2s → cancel, restart 4s

User stops at t=3s → timer fires at t=7s → dropdown appears

In AppE2ETest: void searchDelayShowsSuggestions()

Pokemon API:

Base URL: https://pokeapi.co/api/v2

Auth: none (public API)

Format: JSON over HTTPS

Endpoints:

/pokemon-species/{name} (user picks "Species" from picker)

/evolution-chain/{id} (user picks "Evolution" from picker)

/pokemon/{name} (user picks "Stats" from picker OR browsing grid)

Playwright:

Test class: AppE2ETest.java

Method count: 11 tests

Runtime: ~70 seconds total

@SpringBootTest(webEnvironment = RANDOM_PORT)   ← Starts app on random port
@ActiveProfiles("dev")                          ← Uses H2
@TestMethodOrder(OrderAnnotation.class)         ← Runs @Order(1) → @Order(11)

@BeforeAll launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(headless=true);
}

@BeforeEach createContextAndPage() {
    context = browser.newContext(
        new Browser.NewContextOptions()
            .setRecordVideoDir("target/videos")         ← START RECORDING
            .setRecordVideoSize(1280, 720)
    );
    page = context.newPage();
}

@Test void someTest() {
    page.navigate("http://localhost:" + port + "/");
    page.waitForSelector(".prestamo-card");
    assertThat(page.locator("h1")).containsText("Featured Books");
    screenshot("01-home-page");                          ← SAVES PNG
}

@AfterEach closeContext() {
    context.close();                                     ← STOPS RECORDING, SAVES .webm
}

@AfterAll closeBrowser() { /* cleanup */ }

#	Test	                                        What it clicks	                                                    Proves
1	homePageLoadsAndShowsBooks	                    Loads /	                                                            H2 DB works, Thymeleaf renders
2	bookCardViewModalOpens	                        View button	                                                        REST /api/libros/{id} works
3	searchBarFiltersBooks	                        Types in search	                                                    Client-side filter works (robust version)
4	navigateToPokemonPage	                        Pokémon nav link	                                                Routing works
5	pokemonPickerOpensOnCardClick	                Clicks a card	                                                    Picker modal appears
6	speciesEndpointOpensItsOwnModal	                Picks Species	                                                    /pokemon-species/{name} renders
7	evolutionEndpointOpensItsOwnModal	            Picks Evolution	                                                    /evolution-chain/{id} + parsing
8	statsEndpointOpensItsOwnModal	                Picks Stats	                                                        /pokemon/{name} renders
9	backToPickerFromEndpoint	                    "Back to options"	                                                Modal transitions work
10	evolutionStageClickOpensNewPicker	            Click Ivysaur in chain	                                            Re-modal works for related Pokémon
11	searchDelayShowsSuggestions	                    Types "pikachu", waits 4s	                                        THE 4-SECOND DELAY TEST

What happens:

Playwright launches Chromium (headless).

For each test, @BeforeEach opens a browser context with video recording ON.

The test drives the page (click, type, navigate).

@AfterEach closes the context → .webm file saved to target/videos/.

Result: 11 .webm files (~3–8 MB total), each 10–25 seconds.

Quick reference — where to find each feature:

Feature	                                                            File	                                                    How to find it
4-second delay constant	                                            pokemon.html	                                            Search for SEARCH_DELAY_MS = 4000
4-second delay usage	                                            pokemon.html	                                            Search for setTimeout(() => { renderSuggestions
4-second delay test	                                                AppE2ETest.java Method	                                    searchDelayShowsSuggestions()
Real-time search (no delay)	                                        index.html	                                                searchInput.addEventListener('input', ...)
PokéAPI base URL	                                                pokemon.html	                                            Search for API_BASE
Species endpoint call	                                            pokemon.html	                                            Search for pokemon-species/
Evolution endpoint call	                                            pokemon.html	                                            Search for evolution-chain
Pokemon endpoint call	                                            pokemon.html	                                            Search for pokemon/
H2 config	                                                        application-dev.properties	                                Whole file
Auto-load SQL	                                                    application-dev.properties	                                spring.sql.init.mode=always
Random shuffle	                                                    LibroService.java	                                        Collections.shuffle(all)
REST endpoints	                                                    LibroController.java	                                    Methods starting with api*
Video recording setup	                                            AppE2ETest.java	                                            setRecordVideoDir
Video upload to CI	                                                .github/workflows/playwright-demo.yml	                    actions/upload-artifact@v4
Chromium install	                                                .github/workflows/playwright-demo.yml	                    install --with-deps chromium
