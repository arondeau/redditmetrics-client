# redditmetrics-client

Scatter-plot and trendline for subscriber growth for a particular subreddit. Based on historic data from redditmetrics.com

# Usage

For example: Show scatter-plot and subscriber growth trendline for "IAmA" subreddit in the past 90 days:

```
$ lein repl
...
redditmetrics-client.core=> (show-scatterplot-with-trendline-for-subreddit "IAmA" 90)
```
