BANNER ID TODO      EMAIL TODO

--What is the program for?

This is an implementation of Naive Bayes Classification algorithm, following the original paper http://dl.acm.org/citation.cfm?id=637969
GitHub page of the project: https://github.com/dulljester/Assignment05-NCB

--How to Compile Code?
    compile: make

--How to Run Code?
    run: java Bonus (or java Assignment05)
        NOTE: for simplicity, you can put the dataset inside the directory with the sources (this way you don't have to provide full path)

--Overview of Program code

    --Introduction to the code structure / architecture

        * Classifier: interface with methods trainOnData(), getPrdiction()
        * NaiveBayesClassifier class implements Classifier
        * Summarizer -- private class of NBC, populates the HashMaps with the required marginals
        * There are two main functions: first for the Main Part of Assignment05; the other for the Bonus Part
        * DataHolder class: implements DAO and singleton pattern, same as used in Assignments 03 and 04

    --Specify limitations of the program (if any)
    --Itemset Encoding:

        I use a *long integer* to encode an itemset/transaction. More specifically, assume, for ease of exposition, just two columns
        in the data file. Assume that the first column takes on M values, and the second column takes on N different values.
        Then I would compute m = log2(M) and store an itemset (x,y) as a long integer (x|(y<<m)).
        Each of the different M values are represented by 1..M, with 0 meaning that the item is missing altogether.
        This way, as long as SUM(log_2(V_j)) <= 62, where V_j is the cardinality of the j-th attribute, we can safely
        encode an itemset in Java's built-in long primitive type, or indeed its wrapper class Long, to use with Collections --
        but we don't have to worry about this, since Java takes care of boxing/unboxing.

        This binary-decimal encoding, in addition to being pretty lightweight, allows us to take advantage of bit-parallelism.
        For example, enumerating all subsets of the given itemset is nothing but an enumeration of all the submasks of a given
        bit pattern we will call "signature" -- an integer with bits set to 1 only for positions for which our itemset has
        an entry. Technically, enumerating all the submasks of a bitpattern "base" is accomplished by a simple loop, like so

                    for ( int submask = base; submask > 0; submask = (submask-1) & base )

        Enumerating individual items is accomplished in the number of bits set in "signature" by repeatedly removing a least significant bit,
        like so

                    LSB(x) := (x & (~x+1))

        In short, we harness the power of bitwise operations to facilitate our access to data; all this (and additional) functionality
        is provided by the DataHolder class, which implements a Singleton design pattern (as DAOs, Data Access Objects, in many cases, do).
        Compound bitwise operations such as ones above are implemented in MyUtils class as static members.

        As to incapsulation, all the entities of the program are aware that an itemset is a Long. It is only that DataHolder alone
        knows how to interpret it, and each entity uses its own reference the DataHolder (unique!) object to extract information
        from an itemset (or transaction).


--Structure of Code

    --General Flow of the Algorithm:
        Read the data and put everything inside a DataHolder object (singleton)
        Divide the dataset according to the specified percentage (taken as 70%)
        Count the Priors
        Build the Model
        Classify
        Output the test data together with true labels and classification labels
        Compute the Confusion Matrix
        Output the Confusion Matrix
        Output the Accuracy (correctly classified tuples / all tuples)

    --Modularity/Functionality:

        * MyUtils: utility collection of general-purpose operations and constants
        * DataHolder: interprets the long as a transaction, encodes a transaction as a long, provides access to transaction's fields, loads DB, etc.

        [reusability] I have used the above classes for Assignment 3, 4

        * Classifier: an interface with two methods -- trainOnData() and getPrediction();
        * NaiveBayesClassifier: implements Classifier: essentially, its private inner class Summarizer does all the non-trivial work
          i.e. counting the marginals
        * Bonus: driver; interacts with the user, I/O set-up, etc. Essentially the same as Assignment05 class, but takes only one file
          and divides it in 70/30 ratio for training/testing
        * Assignment05: derives a Bayesian model from training data and classifies another dataset, reports accuracy and predicted and true class labels

    --Code readability and comments

        All the methods bear self-explanatory names and are supplied with asserts to check for pre/post conditions

--Data noise handling
    There is no noise in the data used for Assignment evaluation (data1, data2), as well as for the ones I used for Bonus Part

--Bonus: Description about bonus part
        Please see the assing5.pdf document for the detailed description.

