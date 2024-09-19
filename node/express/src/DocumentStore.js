import * as mysqlx from '@mysql/xdevapi'

class DocumentStore {
    #databaseName
    #collectionName
    #connectionUrl
    #pool
    constructor(dbUser, dbPassword, dbHost, dbPort, dbName, collectionName) {
        this.#databaseName = dbName
        this.#collectionName = collectionName
        this.#connectionUrl =
            `mysqlx://${dbUser}:${dbPassword}@${dbHost}:${dbPort}}/${dbName}`
        this.#pool = mysqlx.getClient(this.#connectionUrl, {
            pooling: {
                enabled: true,
                maxSize: 10,
                maxIdleTime: 20000,
                queueTimeout: 5000
            }
        })
    }

    async listAllScores() {
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find()
            .execute();
        let data = results.fetchAll()
        session.close()
        return data
    }
    async limitAllScores (limit, offset) {
        if(!offset) offset = 0
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find()
            .limit(limit)
            .offset(offset)
            .execute()
        let data = results.fetchAll()
        session.close()
        return data
    }
    async getBestScores(limit){
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find()
            .fields([
                'firstName',
                'lastName',
                'score',
                'date',
                'course']
            )
            .sort(['score asc', 'date desc'])
            .limit(limit)
            .execute()
        let data = results.fetchAll()
        session.close()
        return data
    }
    async getRoundsUnderPar(){
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find("score < course.par")
            .fields(['firstName',
                'lastName',
                'score',
                'date',
                'course.name as courseName'])
            .sort(['date desc'])
            .execute()
        let data = results.fetchAll()
        session.close()
        return data
    }
    async getByScore(score){
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find("score = :scoreParam")
            .bind('scoreParam', score)
            .fields([
                'concat(firstName, " ", lastName) as golfer',
                'score', 'date',
                'course.name as courseName'
            ])
            .sort(['date desc', 'lastName'])
            .execute()
        let scores = results.fetchAll()
        session.close()
        return scores
    }
    async getByGolfer(lastName){
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find("lower(lastName) like :lastNameParam")
            .bind('lastNameParam', lastName.toLowerCase() + '%')
            .fields([
                'concat(firstName, " ", lastName) as golfer',
                'score',
                'date as datePlayed',
                'course.name as courseName',
                'holeScores',
                '_id'
            ]).sort(['date desc'])
            .execute();
        let data = results.fetchAll()
        session.close()
        return data
    }
    async getAverageScorePerGolfer(year){
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find(year ? "year(date) = " + year : 'date is not null')
            .fields(['lastName', 'firstName', 'round(avg(score), 2) as avg', 'count(score) as numberOfRounds'])
            .sort(['lastName', 'firstName'])
            .groupBy(['lastName', 'firstName'])
            .execute()
        let data = results.fetchAll()
        session.close()
        return data
    }
    async getCourseScoringData(){
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        let results = await collection.find()
            .fields([
                'course.name as courseName',
                'round(avg(score), 2)  as avg',
                'min(cast(score as unsigned)) as lowestScore',
                'count(score) as numberOfRounds'
            ])
            .groupBy(['course.name'])
            .sort('course.name desc')
            .execute()
        let data = results.fetchAll()
        session.close()
        return data
    }
    async getHolesInOne(){
        const session = await this.#pool.getSession()

        const sql = `SELECT JSON_OBJECT(
                                'firstName', doc ->> '$.firstName',
                                'lastName', doc ->> '$.lastName',
                                'date', doc ->> '$.date',
                                'courseName', doc ->> '$.course.name',
                                'holes', JSON_ARRAYAGG(
                                        JSON_OBJECT(
                                                'holeNumber', holeScores.number,
                                                'par', holeScores.par
                                            )
                                    )
                            )
                 FROM scores,
                      JSON_TABLE(
                              doc, '$.holeScores[*]'
                              COLUMNS (
                                  score INT PATH '$.score',
                                  number INt PATH '$.number',
                                  par INT PATH '$.par'
                                  )
                          ) holeScores
                 WHERE holeScores.score = 1
                 GROUP by doc ->> '$.lastName', doc ->> '$.firstName'
                 ORDER by doc ->> '$.date' DESC`

        const query = await session.sql(sql)
        let results = await query.execute()
        let data = results.fetchAll()
        session.close()
        return data
    }
    async getAggregateCourseScore(){
        const session = await this.#pool.getSession()
        const sql = `
        WITH aggScores AS
        (SELECT doc ->> '$.course.name' course,
            MIN(score)              minScore,
            MAX(score)              maxScore,
            number
        FROM scores,
            JSON_TABLE(doc, '$.holeScores[*]'
        COLUMNS (
            score INT PATH '$.score',
            number INT PATH '$.number')) AS scores
        GROUP BY course, number
        ORDER BY course, number)
        SELECT JSON_OBJECT('courseName', course, 'bestScore', sum(minScore))
        FROM aggScores
        GROUP BY course
        ORDER BY course;`

        const query = await session.sql(sql)
        let results = await query.execute()
        let data = results.fetchAll()
        session.close()
        return data
    }
    async addScore(score){
        let success = true;
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        try {
            await collection.add(score).execute()
        }
        catch (e) {
            success = false
        }
        session.close()
        return success
    }
    async addHoleScores(data){
        let success = true;
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        try {
            await collection.modify("_id = :idParam")
                .set("holeScores", data.holeScores)
                .bind("idParam", data._id)
                .execute()
        }
        catch (e) {
            success = false
        }
        session.close()
        return success
    }
    async removeScore(id){
        let success = true;
        const session = await this.#pool.getSession()
        const db = session.getSchema(this.#databaseName)
        const collection = db.getCollection(this.#collectionName)
        try {
            await collection.remove("_id = :idParam")
                .bind("idParam", id)
                .execute()
        }
        catch (e) {
            success = false
        }
        session.close()
        return success
    }
}
export default DocumentStore