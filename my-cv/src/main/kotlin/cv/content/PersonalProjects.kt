package cv.content

import cv.dictionaries.Companies
import cv.dsl.CvBuilder
import cv.model.Organization

/** The "Personal projects" section: side projects worth showing off. */
@Suppress("LongMethod") // Declarative CV content is intentionally kept as one section.
internal fun CvBuilder.personalProjectsSection() =
    projects(title = "Personal projects", icon = "faLaptop", webTitle = "Personal Projects") {
        project(
            name = "cv-dsl",
            company = Organization("github.com/tolikttaaa/cv-dsl", url = "https://github.com/tolikttaaa/cv-dsl"),
            dates = "2026",
            tags = listOf("Kotlin", "Kotlin DSL", "Gradle Plugin", "LuaLaTeX", "HTML", "JUnit 5", "GitHub Actions"),
        ) {
            paragraph(
                """
                Designed and built an open-source, type-safe Kotlin DSL that keeps a CV in code and renders one model
                as a LuaLaTeX PDF, a static web portfolio and Markdown, with a Gradle plugin for generation, PDF
                compilation and local preview. It generates this CV and its portfolio site.
                """,
            ) {
                highlight("this CV and its portfolio site", linkTo("https://tolikttaaa.github.io/personal-cv/"), bold)
            }
            bullets {
                item(
                    """
                    Verifies the PDF layout at build time: the page limit and per-section page rules are checked
                    against the pages recorded during LaTeX compilation.
                    """,
                )
            }
        }
        project(
            name = "System of retryable chain tasks",
            company = Companies.TINKOFF,
            dates = "2022",
            tags = listOf("Kotlin", "Spring", "Task Scheduler", "Quartz"),
        ) {
            paragraph(
                """
                Implemented a Java Spring library for delayed and retryable execution of task chains (similar
                to Quartz), supporting configurable retries, delays, metrics, and REST-based task management.
                """,
            ) {
                bold("Quartz")
            }
            bullets {
                item("Job success rate increased to 99.9% and recovery time reduced by 50%.") {
                    bold("increased to 99.9%")
                    bold("reduced by 50%")
                }
            }
        }
    }
