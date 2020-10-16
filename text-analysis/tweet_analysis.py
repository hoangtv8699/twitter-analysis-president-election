from textblob import TextBlob


def getPolarity(text):
    return TextBlob(text).sentiment.polarity


def getSubjectivity(text):
    return TextBlob(text).sentiment.subjectivity


def getAnalysis(score):
    if score < 0:
        return 'Negative'
    elif score == 0:
        return 'Neutral'
    else:
        return 'Positive'


def sentiment_analysis(tweets):
    # get sentiment negative, neutral or positive
    tweets['polarity'] = tweets['text'].apply(getPolarity)
    tweets['analysis'] = tweets['polarity'].apply(getAnalysis)
    return tweets


def drop_neutral(tweets):
    # drop neutral sentiment
    neutral = tweets[tweets['polarity'] == 0.0000]
    drop_pos = tweets['polarity'].isin(neutral['polarity'])
    tweets.drop(tweets[drop_pos].index, inplace=True)
