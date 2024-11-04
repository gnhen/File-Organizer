# Java File Organizer

A user-friendly Java application designed to organize files within a specified directory by sorting them into categories based on their extensions. The Java File Organizer helps maintain a clean file structure by categorizing files into folders such as **IMAGES**, **VIDEOS**, **AUDIO**, **DOCUMENTS**, **ARCHIVES**, and **CODE**, while leaving any subdirectories untouched.

## Features

- **Categorization**: Sorts files into the following categories based on their extensions:
  - **IMAGES**: jpg, jpeg, png, gif, bmp, tiff, webp, svg, ico, raw
  - **VIDEOS**: mp4, avi, mkv, mov, wmv, flv, webm, m4v, mpeg, mpg
  - **AUDIO**: mp3, wav, m4a, flac, aac, wma, ogg, mid, midi
  - **DOCUMENTS**: pdf, doc, docx, txt, rtf, odt, xls, xlsx, ppt, pptx
  - **ARCHIVES**: zip, rar, 7z, tar, gz, bz2, iso
  - **CODE**: java, py, cpp, c, h, js, html, css, php, rb
- **Directory Preservation**: The organizer will not affect any subdirectories, ensuring they remain untouched.
- **User Interface**: Provides a graphical interface with easy navigation and progress tracking.

## Getting Started

### Prerequisites

- Java 8 or higher installed on your system.

### Downloading the .jar File

1. Go to the [Releases](https://github.com/gnhen/File-Organizer/releases) section of this repository.
2. Find the latest release and click on the `.jar` file (e.g., `FileOrganizer.jar`) to download it.

### Running the Application

To run the application, open your command line or terminal and navigate to the directory where you downloaded the `.jar` file. Use the following command:

```bash
java -jar FileOrganizer.jar
```

### Example

Simply execute:

```bash
java -jar FileOrganizer.jar
```

Upon launching, you will be presented with a graphical user interface.

## Using the Application

1. **Select a Directory**: Click the **Browse** button to select the directory you wish to organize.
2. **Scan File Types**: Click the **Scan Types** button to scan for unique file extensions in the selected directory. The application will populate the table with the detected file types and their corresponding categories.
3. **Organize Files**: Once the file types have been scanned, click the **Organize Files** button to categorize and move files into their respective folders. The progress will be displayed, and a log of actions will be shown.

## Contact

For any questions or feedback, feel free to open an issue in this repository.
