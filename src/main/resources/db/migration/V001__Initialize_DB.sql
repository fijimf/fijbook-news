CREATE TABLE rss_feed (
                          id BIGSERIAL NOT NULL,
                          name VARCHAR(36) NOT NULL,
                          url VARCHAR(256) NOT NULL
);
CREATE UNIQUE INDEX ON rss_feed(url);
CREATE UNIQUE INDEX ON rss_feed(name);

CREATE TABLE rss_item (
                          id BIGSERIAL NOT NULL,
                          feed_id BIGINT NOT NULL,
                          title VARCHAR(144) NOT NULL,
                          url VARCHAR(256) NOT NULL,
                          image VARCHAR(256) NULL,
                          published_at TIMESTAMP NOT NULL,
                          retrieved_at TIMESTAMP NOT NULL,
                          verified_at TIMESTAMP NULL,
                          status_code INT NULL,
                          response_time INT NULL,
                          response_size INT NULL
);
CREATE UNIQUE INDEX ON rss_item(feed_id, url);

CREATE TABLE rss_refresh_job (
                                 id BIGSERIAL NOT NULL,
                                 feed_id BIGINT NOT NULL,
                                 start_time TIMESTAMP NOT NULL,
                                 end_time TIMESTAMP NOT NULL,
                                 status_code INT NOT NULL,
                                 item_count INT NOT NULL,
                                 new_item_count INT NOT NULL
);


