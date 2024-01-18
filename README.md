# eScalaDe

## Overview

This application is a ZIO-based Scala project designed to process and analyze transaction data from the real estate
market. It fetches data from a specified URL, decompresses it if necessary, parses the data, and applies various filters
to extract meaningful insights.

## Features

- Environment Variable Loading: Utilizes the Dotenv library to load essential configurations.\
- Data Fetching: Retrieves data from a given URL using an HTTP client within the ZIO environment.\
- Data Decompression: Handles gzipped data streams.\
- CSV Parsing: Parses CSV data into Transaction objects.\
- Data Filtering: Applies filters to transaction data based on user input.\
- Metrics Computation: Calculates various metrics from the filtered data.\

## Prerequisites

- Scala
- sbt (Scala Build Tool)
- A `.env` file containing `DATA_URL, START_YEAR, END_YEAR`, and optionally `CSV_SEPARATOR`.
- Installation
  Clone the repository:

```bash
git clone [repository-url]
```

Navigate to the project directory:

```bash
cd [project-directory]
```

Compile the project using sbt:

```bash
sbt run
```

Follow the prompts to input filters and view the computed metrics based on the transaction data.

## Development

### Key Components

loadEnvVars: Loads necessary environment variables.\
fetchData: Fetches and processes data from a URL.\
decompressGzippedData: Decompresses gzipped data streams.\
parseCsvLine: Parses a line of CSV into a Transaction object.\
validateTransaction: Validates and filters transactions.\
loadTransactions: Loads transactions for a range of years.\
computeMetrics: Computes various metrics from transaction data.\
getUserFilters: Interactively gets user-specified filters.\
filterTransactions: Applies filters to transaction streams.\

### Adding New Features

Ensure new features are well-integrated with the existing ZIO environment.\
Follow the established pattern for error handling and resource management.\

### Testing

Run unit tests using sbt:

```bash
sbt test
```