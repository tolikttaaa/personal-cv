Generate the CV artifacts from the Kotlin DSL and compile the PDF. All artifacts go to the `build/` directory.

Run:

```
./gradlew generatePdf
```

Run it from the repository root. This runs the `generateLatex` task (Kotlin DSL → build/latex), compiles with two LuaLaTeX passes → build/cv.pdf, and checks the PDF layout rules (at most 2 pages, Experience on page 1). The LuaLaTeX output is captured in build/lualatex.log; the pages of every section and entry are listed in build/cv-layout.txt.

To also regenerate the web data, run `./gradlew generateWeb`, or `./gradlew run` for both.

Report success or any errors (check build/lualatex.log on compilation failure; on a layout rule failure, report the violated rules and the relevant lines of build/cv-layout.txt).
