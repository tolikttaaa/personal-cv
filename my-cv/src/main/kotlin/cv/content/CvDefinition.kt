package cv.content

import cv.dsl.cv
import cv.model.Cv

/**
 * The single source of truth for the CV content.
 *
 * The header (name, tagline, contacts) lives here; each section body lives in
 * its own file in this package, mirroring the section order of the document:
 *
 *  - [summarySection]   — Summary.kt
 *  - [experienceSection] — Experience.kt
 *  - [skillsSection]    — Skills.kt
 *  - [personalProjectsSection] — PersonalProjects.kt
 *  - [teachingSection]  — Teaching.kt
 *  - [educationSection] — Education.kt
 *  - [referencesSection] — References.kt
 *
 * Body text (paragraphs, bullet items) is written as plain multiline strings —
 * indentation and line breaks collapse to single spaces, and special characters
 * are escaped automatically. Formatting is declared separately as highlight
 * rules matching a literal substring or a Regex:
 * ```
 * paragraph("…text mentioning Azul and ITMO University…") {
 *     bold("Azul")                                        – single style
 *     highlight("ITMO University", linkTo(url), bold)     – combined styles
 *     italic(Regex("[Cc]ontinuous \\w+"))                 – regex match
 * }
 * ```
 * Available styles: bold, italic, nowrap, colored(CvColor.X), linkTo(url).
 * A rule that matches nothing fails the build.
 *
 * Page rules keep the PDF in shape: `pdf { maxPages }` bounds its length, and
 * any section or entry can take `pageFit = PageFit.OnPage(n)` (entirely on
 * page n) or `PageFit.SinglePage` (never split across pages).
 */
val anatoliiCv: Cv = cv {
    firstName = "Anatolii"
    lastName = "Anishchenko"
    tagline = "Java/Kotlin Software Engineer"
    photo(file = "photo.jpg", size = "2.2cm")
    footerText = "Anatolii Anishchenko — CV"
    hyphenation = false // words always wrap whole; no per-word nowrap needed

    // PDF print settings. Every font size scales with fontSize. generatePdf
    // fails when the PDF exceeds maxPages or breaks a section's pageFit rule
    // (Experience must stay on page 1), listing each violation and the pages
    // every element landed on (build/cv-layout.txt).
    pdf {
        fontSize = 9.0
        maxPages = 2
    }

    social {
        row {
            phone("+357 974 33 973")
            email("tolik.ttaaa@gmail.com")
            telegram("ttaaa_work")
        }
        row {
            linkedin("ttaaa")
            github("tolikttaaa")
            leetcode("ttaaa")
        }
        row {
            address("Cyprus, Limassol")
        }
    }

    summarySection()
    experienceSection()
    skillsSection()
    personalProjectsSection()
    teachingSection()
    educationSection()
    referencesSection()
}
