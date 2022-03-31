

# Contributing

Disclaimer: The maintainers of this repository are newbies when it comes to hosting open source repositories so don't be offended if we don't follow common practice! Help us be better!


If you want to contribute, feel free to make a fork and pull request towards the develop branch. Please prepare benchmark tests that showcase you proposed changes. You can use [PngEncoderBenchmarkPngEncoderVsImageIO](https://github.com/pngencoder/pngencoder/blob/develop/src/test/java/com/pngencoder/PngEncoderBenchmarkPngEncoderVsImageIO.java) but feel free to come up with something that better shows your progress!

Remember that the goal for this project is processing speed without the cost of quality. We don't care as much about the file sizes or memory consumption withing reason, of course.





# Code Style Guidelines

This is a set of guidelines that we have agreed upon so far. Over time we will add to and update these guidelines. However, we will never get a complete set of guidelines covering all cases. The focus for this document is to pin down some of the main points where there could be differences in opinions and this could lead to uncertainties during code reviews. As a base we follow the standard [Java coding conventions.](https://www.oracle.com/technetwork/java/codeconventions-150003.pdf)

## Code Style
* Braces
  * The starting `{` should **not** be on a new line
  * Always use `{...}` for blocks, even if they only contain a single line.
  No if-statements without `{...}`. [Example of why](https://www.imperialviolet.org/2014/02/22/applebug.html).
* Import statements
  * Avoid "star imports" and use explicit imports instead.
* `final`
  * Avoid pointless finals in methods.
  * Use `final` on fields as much as possible. Even though it doesn't guarantee immutability (since the data inside the object might still change), it's one step closer.
* `Objects.requireNonNull()`
  * Avoid `Objects.requireNonNull` in constructors that are auto-wired.
  * It still makes sense to use it for data classes where data is validated.
* Annotations
  * `Objects.requireNonNull()` (above) is very verbose, annotations are less so, and they can potentially provide more value - both to the runtime and for static code analysis. 
  * Use your own judgement when placing annotations. The goal should be to make the code more readable.
  * `@Inject` is implied in newer Spring versions. Avoid unnecessary runtime annotations.
  * Some annotations e.g. `@Transactional` require the method to be public (and not final). Keep this in mind when refactoring. IntelliJ will happily make mistakes for you. 
* Code comments
  * Don't leave commented out code in the code base. Remove the code. The old code is available in git history.
  * Don't add empty Javadoc comments in the code.
    * Some IDEs automatically add comments for methods etc. These rarely add value and are mostly just noise in the code.
  * Add comments for complex parts of the code. Let the comments explain things that are not obvious from reading the code.
  * Make sure to remember to update or remove any existing comments when changing code. No comments is better than wrong comments.
* Line length
  * No exact number for the maximum line length has been agreed upon yet. 

## Commit Style
* One commit - one thing
  * A commit should be atomic in the sense that it addresses one concern. One bugfix, one feature implementation, one refactoring etc.
  * Style changes, refactoring, formatting etc should not be included in the same commit as functional code changes.
  * It is fine to fix style issues etc in the code, but do it as separate commits.
* Commit messages
  * Use short and descriptive messages.
  * Try to stay below 50 characters.
    * Add a blank line and then write more text if there is more to describe than fit in the first 50 characters.
  * Seven rules of a great commit message (from [this article](http://chris.beams.io/posts/git-commit/)):
    * [Separate subject from body with a blank line](https://chris.beams.io/posts/git-commit/#separate)
    * [Limit the subject line to 50 characters](https://chris.beams.io/posts/git-commit/#limit-50)
    * [Capitalize the subject line](https://chris.beams.io/posts/git-commit/#capitalize)
    * [Do not end the subject line with a period](https://chris.beams.io/posts/git-commit/#end)
    * [Use the imperative mood in the subject line](https://chris.beams.io/posts/git-commit/#imperative)
    * [Wrap the body at 72 characters](https://chris.beams.io/posts/git-commit/#wrap-72)
    * [Use the body to explain what and why vs. how](https://chris.beams.io/posts/git-commit/#why-not-how)

## Exceptions
* When catching and re-throwing `IOException`, use [`UncheckedIOException`](https://docs.oracle.com/javase/9/docs/api/java/io/UncheckedIOException.html) rather than `RuntimeException`.

