# fijbook-news
News scraping subproject

* `GET /feeds`
* `GET /feeds/:feedId`
* `GET /feeds/:feedId/items    [after=yyyyMMddHHmmss] [skipMissing] [skipUnverified]`
* `GET /feeds/:feedId/items/:itemId`
* `POST /feeds`
* `DELETE /feeds/:feedId`
* `GET /items    [after=yyyyMMddHHmmss] [skipMissing] [skipUnverified]`
* `GET /items/:itemId`
* `DELETE /items/:itemId`
* `POST /refresh/:feedId`
* `POST /verify/:feedId    [after=yyyyMMddHHmmss] [skipMissing] [skipUnverified]`
* `GET /refreshJobs`
* `GET /refreshJobs/:feedId`
