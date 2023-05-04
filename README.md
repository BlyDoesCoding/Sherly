
# Sherly
Sherly is a Multithreaded Duplicate File Finder for your Terminal, written in java. You can Easily find duplicate Images, videos as well as any other type of Data. That can be helpful if you run on small storage or just want to keep regular housekeeping.


## Installation

Install via AUR

```bash
  yay -S sherly-git
```

Compile yourself

```bash
  git clone https://github.com/BlyDoesCoding/Sherly
  cd Sherly
  mkdir -p Bin
  javac -d Bin src/*.java -Xlint
  cd Bin
  jar cfe sherly.jar Main ConsoleColors.class Main.class ThreadedCompare.class
  #the jar file will be in the Bin Folder
```


## Usage/Examples

```bash
Usage: sherly -f inputfolder1 inputfolder2 inputfolder3 [options]...

   -h / -help             show this
   -f / -folder           all the folders you want to scan for (see example above!)
   -c / -color            enable colored messages
   -t / -threads          override default Thread number (default is usually number of cores * 2)
   -p / -progress         enable progress indicator
   -d / -delete           delete all dups except one without asking first
   -debug                 debug stuff
```

