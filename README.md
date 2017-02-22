# redditmetrics-client

Scatter-plot and trendline for subscriber growth for a particular subreddit. Based on historic data from redditmetrics.com

# Usage

![IAmA Trendline](https://raw.githubusercontent.com/arondeau/redditmetrics-client/master/iama365.png)

For example: Show scatter-plot and subscriber growth trendline for "IAmA" subreddit in the past 365 days:

```
$ lein repl
...
redditmetrics-client.core=> (show-scatterplot-with-trendline-for-subreddit "IAmA" 365)
```
