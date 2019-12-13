Quiver Results Converter
==========

Quiver Results Converter is a CLI utility that converts results from performance tool [Quiver](https://github.com/ssorj/quiver) to Maestro Data Format used in performance tool [Maestro](https://github.com/maestro-performance/maestro-java).

This utility is used by [maestro-quiver-agent](https://github.com/maestro-performance/maestro-quiver-agent).

Getting Started
-----

You can find latest tarballs in the Releases menu [here](https://github.com/dstuchli/quiver-results-converter/releases).
After extracting the tarball you can find the run script in `bin/` folder.

Basic usage of the run script:
```
    ./quiver-results-converter.sh <Action>

    Actions:
        convert <arguments>
            Arguments being the .csv.xz file and .json file. Both with sender or both with receiver prefix.
        help
        version
```

License
-----

This project is licensed under the ASL 2.0 License - see the [LICENSE](LICENSE) file for details

Acknowledgments
-----

* [PurpleBooth](https://github.com/PurpleBooth) -- [readme template](https://gist.github.com/PurpleBooth/109311bb0361f32d87a2)