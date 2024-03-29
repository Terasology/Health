<div align="center">
<img src="https://raw.githubusercontent.com/Terasology/Health/develop/docs/_media/banner.png">
</div>

_This is a module for [Terasology].
It adds the concept of health (or hitpoints) to the game and handles regeneration, restoration and damage of entities._

<h2 align="center"><a href="https://terasology.github.io/Health">👉 Documentation 👈</a></h2>

## Contributing

We welcome contributions to our modules, be it bug fixes or feature contributions. 
Check out the [Contributor Guide][contributor-guide] on the main project wiki to learn more.

To check out this module (and all its dependencies) to your Terasology workspace run (in the workspace root):

```
groovyw module recurse Health
```

To build a module JAR for just this module run (in the workspace root):

```
gradlew :module:Health:jar
```

To run all tests and static code checks for this module run (in the workspace root):

```
gradlew :module:Health:check
```

### Documentation via gh-pages

The documentation of this module is build with [docsify]. 
It is served via [gh-pages].
To preview the site you can either use the `docsify` [CLI tool](https://github.com/docsifyjs/docsify-cli) or just run a static server on the `docs` folder.

<!-- References -->
[Terasology]: https://github.com/MovingBlocks/Terasology
[gh-pages]: https://pages.github.com/
[docsify]: https://docsify.js.org/#/
[contributor-guide]: https://github.com/MovingBlocks/Terasology/wiki/Contributor-Quick-Start
