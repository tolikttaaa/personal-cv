# Anatolii Anishchenko — CV

A Kotlin DSL is the single source of truth for this CV. The project renders the
same immutable model into:

- a LuaLaTeX document and PDF, at most two pages long;
- a static portfolio site with no intermediate JSON or runtime data fetch.

GitHub Actions verifies the Kotlin code, compiles the PDF, checks its page
layout, assembles the site, and deploys it to GitHub Pages:
<https://tolikttaaa.github.io/personal-cv/>.

## Project layout

```text
├── gradle.properties               # Pinned cv-dsl release
├── gradle/libs.versions.toml       # Build-tool versions
├── my-cv/                          # This CV's content and generation entry point
│   └── src/main/
│       ├── kotlin/cv/
│       │   ├── content/            # Header and section definitions
│       │   └── dictionaries/       # Organizations and institutions used by this CV
│       └── resources/photo.jpg     # Profile photo
├── build/                          # Generated artifacts; never edited manually
└── .github/workflows/              # Verification and Pages deployment
```

The reusable implementation lives in the separate
[`tolikttaaa/cv-dsl`](https://github.com/tolikttaaa/cv-dsl) repository. This
project consumes its tagged JitPack artifact for both the Gradle generation
plugin and the application API; only personal content remains here.

## Rendering architecture

The render layer keeps the model independent from HTML and LaTeX:

1. `CvRenderer` defines the public `render(cv, outputDirectory)` operation.
2. `RenderFormat` is the closed set of supported formats: `Web` and `Latex`.
3. `CvRendererFactory` selects the complete renderer for a format.
4. `RendererBundle<C>` is the composition root for a format. It requires a
   renderer for every supported section and leaf element.
5. `SectionRenderer<C>` performs exhaustive dispatch over the sealed section
   hierarchy.
6. `ElementRenderer<E, C>` is implemented by format-specific renderers such as
   `WebProjectRenderer` and `LatexProjectRenderer`.

```kotlin
val renderer = CvRendererFactory.create(RenderFormat.Web)
renderer.render(cv, outputDirectory)
```

`CvApplication` sits above the individual renderers. It parses the common CLI,
selects formats, owns the output layout, and copies content assets from an
injectable `CvAssetSource`. The consumer entry point therefore only supplies
its model:

```kotlin
fun main(args: Array<String>) = CvApplication(anatoliiCv).run(args)
```

By default, a photo declared as `photo("photo.jpg")` is loaded from the
consumer's runtime classpath. Consumers with another asset store can pass a
custom `CvAssetSource` without changing the generation pipeline.

Both `WebRendererBundle` and `LatexRendererBundle` must satisfy the same bundle
contract. If a required renderer is missing, compilation fails. Web renderers
receive `WebRenderContext`; LaTeX renderers use `Unit` because section rendering
does not require shared state.

Library architecture, extension rules and required test coverage are documented
in the standalone project's
[`CONTRIBUTING.md`](https://github.com/tolikttaaa/cv-dsl/blob/main/CONTRIBUTING.md)
and [`architecture.md`](https://github.com/tolikttaaa/cv-dsl/blob/main/docs/architecture.md).

## Gradle generation plugin

`cv-dsl` defines the `cv.dsl.generation` plugin alongside the model and
renderers. This project loads both from one tagged JitPack artifact:

```kotlin
buildscript {
    repositories { maven("https://jitpack.io") }
    dependencies { classpath("com.github.tolikttaaa:cv-dsl:<tag>") }
}

plugins {
    kotlin("jvm")
    application
}

apply(plugin = "cv.dsl.generation")

dependencies {
    implementation("com.github.tolikttaaa:cv-dsl:<tag>")
}

application {
    mainClass.set("cv.MainKt")
}
```

The plugin expects that main class to accept `[repositoryRoot, target]`, where
the target is `latex` or `web`. It owns the complete pipeline:

- `verifyCvEnvironment` checks LuaLaTeX and the JDK `jwebserver` executable;
- `generateLatex` and `generateWeb` invoke the consumer's generator;
- `generatePdf` verifies tools, generates LaTeX, runs LuaLaTeX twice, and
  checks the PDF against its [layout rules](#pdf-layout);
- `assembleSite` combines generated web files and the PDF;
- `serveSite` and `stopSite` manage a PID-tracked local preview process.

Optional consumer configuration:

```kotlin
cvGeneration {
    mainClass.set("example.MainKt")
    lualatexExecutable.set("/opt/texlive/bin/lualatex")
    previewPort.set(9090)
}
```

Command-line overrides are also available:

```sh
./gradlew generatePdf -PlualatexPath=/absolute/path/to/lualatex
./gradlew serveSite -PcvPreviewPort=9090
```

Missing tools and occupied ports fail early with installation or override
instructions. Preview management uses only JDK APIs; it does not require
`bash`, `lsof`, or `xargs`.

## Editing the CV

Personal content lives in `my-cv/src/main/kotlin/cv/content/`:

- `CvDefinition.kt` defines identity, photo, contacts, section order and footer;
- the remaining files define one section each.

Rich text is written as plain content and styled through matching rules:

```kotlin
paragraph(
    """
    Software Engineer experienced with Kotlin and ITMO University.
    """,
) {
    bold("Kotlin")
    highlight("ITMO University", linkTo("https://en.itmo.ru/"), bold)
}
```

Supported styles include `bold`, `italic`, `nowrap`, `colored(...)`, and
`linkTo(...)`. A highlight rule that matches nothing fails generation, which
prevents formatting from silently disappearing after text changes. Renderers
escape HTML and LaTeX special characters automatically.

Contacts are declared in visual rows. The web representation flattens those
rows into its contact card, while LaTeX preserves the row boundaries:

```kotlin
social {
    row {
        linkedin("ttaaa")
        github("tolikttaaa")
        leetcode("ttaaa")
    }
}
```

### PDF layout

The PDF must stay within two pages, with the whole Experience section on page 1.
Both are declared in the content and checked on every PDF build:

```kotlin
pdf {
    fontSize = 9.0 // pt; every other size scales with it
    maxPages = 2
}

experience(title = "Experience", icon = "faSuitcase", id = "experience", pageFit = PageFit.OnPage(1)) { … }
```

Any section or entry accepts `pageFit = PageFit.OnPage(n)` (entirely on page
`n`) or `PageFit.SinglePage` (never split by a page break). When a change breaks
a rule, `generatePdf` fails and lists each violation:

```text
PDF layout: 3 pages, 2 of 2 page rules violated:
  - The PDF has 3 pages, but is limited to 2
  - Experience must fit on page 1, but occupies pages 1–2
```

`build/cv.pdf` is still written for inspection, and `build/cv-layout.txt` shows
the page of every section and entry. Shorten the content or lower `fontSize`
until the rules hold again.

## Building locally

Requirements:

- JDK matching the `java` version in `gradle/libs.versions.toml`;
- LuaLaTeX from TeX Live 2022 or newer for PDF generation. Web-only generation
  does not require a TeX installation.

Important tasks:

| Task | Purpose | Output |
|---|---|---|
| `check` | Compile, test and run Detekt in both modules | Verification result |
| `detekt` | Run static analysis only | `<module>/build/reports/detekt/` |
| `verifyCvEnvironment` | Check LuaLaTeX and `jwebserver` | Diagnostic output |
| `generateLatex` | Render the DSL to LaTeX sources | `build/latex/` |
| `generatePdf` | Render and compile the PDF in two passes, check its layout rules | `build/cv.pdf`, `build/cv-layout.txt` |
| `generateWeb` | Render complete HTML and extract browser assets | `build/web/` |
| `assembleSite` | Combine the portfolio, photo and PDF | `build/site/` |
| `serveSite` | Assemble and serve the site on port 8080 | Local HTTP server |
| `stopSite` | Stop the local server | — |

```sh
./gradlew check
./gradlew generatePdf
./gradlew serveSite
./gradlew stopSite
```

Run `./gradlew tasks --group cv` for the CV-specific task list. The application
entry point also supports direct generation:

```text
Main [repository-root] [latex|web|all]
```

If LuaLaTeX is not installed at the standard MacTeX path and is not available
on `PATH`, override it with `-PlualatexPath=/absolute/path/to/lualatex`.

The preview server uses the JDK's `jwebserver`, records its PID in
`build/site-server.pid`, and writes output to `build/site-server.log`.

### Developing against a local cv-dsl

Changes that need a new library feature can be built against a local checkout
of [`cv-dsl`](https://github.com/tolikttaaa/cv-dsl) before it is released:

```sh
./gradlew generatePdf -PcvDslPath=../cv-dsl
```

The included build replaces the pinned JitPack artifact on both the plugin
classpath and the application classpath.

## Version management

`gradle.properties` pins one `cvDslVersion` for both the build-script plugin
classpath and the application dependency. It normally names a release tag;
update it only after the standalone repository's release CI is green and its
immutable JitPack artifact is available. While a cv-dsl pull request is under
review, a branch here may pin that PR's commit hash instead, which JitPack
builds on demand. Build-tool versions remain in `gradle/libs.versions.toml` and
follow the versions cv-dsl is built with.

Detekt runs with its default rule set, reports in Checkstyle, HTML, SARIF and
Markdown formats, and is part of every `check` invocation. Narrow suppressions
are used only for intentionally declarative DSL structures.

## Deployment

Every pull request, push to `main` and manual dispatch runs, inside a full
TeX Live container:

1. `./gradlew check`;
2. generation of LaTeX and the static portfolio;
3. PDF compilation with LuaLaTeX and verification of its layout rules;
4. assembly of `build/site`.

The PDF, its layout report and the LuaLaTeX log are uploaded as the
`cv-layout` artifact. Pushes to `main` then deploy `build/site` to the
`gh-pages` branch, which GitHub Pages serves from its root.
