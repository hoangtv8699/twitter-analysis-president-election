import csv
import tweepy
import ssl

# Oauth keys
consumer_key = "QN3CzI2gScYvDsrhhaL2SRbOPrC"
consumer_secret = "AQU3NwlOqUb1aKxgy0Nk22H5k8jjj0tYJ4nlFRLFZQJCA07TLCJMm"
access_token = "969527167221563392-35WKxHqmuLkkqfe1zqQbmSN276vZTFAbz"
access_token_secret = "wplE6EPMtyqNRESaBV175jRzU5ffgq934nX3h2dNQ7rnzarg"

# Authentication with Twitter
auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_token_secret)
ssl._create_default_https_context = ssl._create_unverified_context
api = tweepy.API(auth)
api = tweepy.API(auth, wait_on_rate_limit=True)

user = api.me()
print(user.name)

# update these for the tweet you want to process replies to 'name' = the account username and you can find the tweet id within the tweet URL
name = 'realDonaldTrump'
tweet_id = ['1290967953542909952']

replies = []
for tweet in tweepy.Cursor(api.search, q='to:' + name, result_type='recent', timeout=999999).items(100):
    if hasattr(tweet, 'in_reply_to_status_id_str'):
        if tweet.in_reply_to_status_id_str == tweet_id:
            replies.append(tweet)

with open('trump_data.csv', 'a+') as f:
    csv_writer = csv.DictWriter(f, fieldnames=('user', 'text'))
    csv_writer.writeheader()
    for tweet in replies:
        row = {'user': tweet.user.screen_name, 'text': tweet.text.replace('\n', ' ')}
        csv_writer.writerow(row)
