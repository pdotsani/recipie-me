# Recipe Me

## Overview
This app allows you to generate recipes. There are two ways
to generate recipes: by name or by ingredients. We use the 
OpenAI API to gather all the data.

## Running the project
To run the project, see [documentation](./vaadin.md)  
  
Add a openAPI key in an `.env` file in the root directory:
```
OPEN_AI_KEY=<key-here>
```

## Development Process
I first started with several prompts to gather the data for each type
of recipe fetching. For recipe name, the prompts were direct. For ingredients,
I had to chain the prompts to gather a result from a result. For images, I tried
a different approach to the API and made an https request.  
  
- To parse the markdown, I used the 3rd party library commonmark.  
- To make the http request, I used okhttp.
- JSON handling was mostly done using gson.

## Test Plan
I tested various scenarios and determined an ideal wait time for the api.