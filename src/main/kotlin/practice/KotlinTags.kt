package practice

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

object KotlinTags {
    /**
     * Practice: HTML rendering library
     * KotlinTags
     *
     * Steps
     * 1) - Define data types for the HTML tags we want to support
     *  - html, head, title, body, div, p
     * 2) Define some "builders" that enable the DSL for every tag we want to support
     *  - HtmlBuilder, HeadBuilder, BodyBuilder, DivBuilder
     * 3) Define methods that take lambdas with receivers as arguments -> build the DSL
     * 4) Test that it works
     *
     */

    sealed interface HtmlElement

    // Step 1 - define data types
    data class Html(val head: Head, val body: Body): HtmlElement {
        override fun toString(): String =
            "<html>\n$head\n$body\n</html>"
    }

    data class Head(val title: Title): HtmlElement {
        override fun toString(): String =
            "<head>\n$title\n</head>"
    }

    data class Title(val content: String): HtmlElement {
        override fun toString(): String =
            "<title>$content</title>"
    }

    data class Body(val children: List<HtmlElement>): HtmlElement {
        override fun toString(): String =
            children.joinToString( "\n", "<body>", "</body>" )
    }

    data class Div(val children: List<HtmlElement>, val id: String? = null, val className: String? = null):
        HtmlElement {
        override fun toString(): String {
            val idAttr = id?.let {  " id=\"$it\"" } ?: ""
            val classAttr = className?.let {  " class=\"$it\"" } ?: ""
            val innerHtml = children.joinToString("\n")
            return "<div$idAttr$classAttr>$innerHtml</div>"
        }
    }

    data class P(val content: String): HtmlElement {
        override fun toString(): String = "<p>$content</p>"
    }

    // step 2 - define the "builders" for the types we want to support
    class HtmlBuilder {
        private lateinit var head: Head
        private lateinit var body: Body

        fun head(init: HeadBuilder.() -> Unit) {
            val builder = HeadBuilder()
            builder.init()
            head = builder.build()
        }

        fun body(init: BodyBuilder.() -> Unit) {
            val builder = BodyBuilder()
            builder.init()
            body = builder.build()
        }

        fun build(): Html =
            Html(head, body)
    }

    class HeadBuilder {
        private lateinit var title: Title
        fun title(content: String) {
            title = Title(content)
        }

        fun build(): Head =
            Head(title)
    }

    class BodyBuilder {
        private val children = mutableListOf<HtmlElement>()

        fun div(id: String? = null, className: String? = null, init: DivBuilder.() -> Unit): Div {
            val builder = DivBuilder(id, className)
            builder.init()
            return builder.build()
        }

        fun p(content: String) {
            children.add(P(content))
        }

        fun build() = Body(children)

    }

    class DivBuilder(val id: String?, val className: String?) {
        private val children = mutableListOf<HtmlElement>()
        fun paragraph(content: String) {
            children.add(P(content))
        }

        // expose a "build" method to give me back the final data structure
        fun build() = Div(children, id, className)
    }

    // step 3 - Define methods that take lambdas with receivers as arguments -> build the DSL
    fun html(init: HtmlBuilder.() -> Unit): Html {
        val builder = HtmlBuilder()
        builder.init()
        return builder.build()
    }

    // step 4 - an example to test that it works
    val htmlExample =
        html {
            head {
                title("my web page")
            }
            body {
                div(id = "header", className = "main-header") {
                    p("welcome to my web site")
                }
                div {
                    p("this is the start of my website")
                    p("this was rendered with KotlinTagzzzz")
                }
            }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        val pw = PrintWriter(FileWriter(File("src/main/resources/sample.html")))
        pw.println(htmlExample)
        pw.close()
    }

}