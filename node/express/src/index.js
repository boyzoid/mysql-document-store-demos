// importing the dependencies
import express from 'express'
import * as dotenv from 'dotenv'
dotenv.config()

// Document Store Service
import DocumentStore from "./DocumentStore.js";
const docStore = new DocumentStore(
            process.env.DB_USER,
            process.env.DB_PASSWORD,
            process.env.DB_HOST,
            process.env.DB_PORT,
            process.env.DB_NAME,
            process.env.COLLECTION_NAME
);


// defining the Express app
const app = express()

// using bodyParser to parse JSON bodies into JS objects
app.use(express.json())

// starting the server
app.listen(process.env.PORT, () => {
    console.log('listening on port ' + process.env.PORT)
});

// defining the default endpoint
app.get('/', (req, res) => {
    let msg = {message: 'Node Demo main endpoint'}
    res.send(msg)
});

// /list endpoint
app.get('/list/', async (req, res) => {
    const scores = await docStore.listAllScores()
    let msg = {count: scores.length, scores: scores}
    res.send(msg)
})

// /list with limit
app.get('/list/:limit/:offset?', async (req, res) => {
    const scores = await docStore.limitAllScores(req.params.limit, req.params.offset)
    let msg = {count: scores.length, scores: scores}
    res.send(msg)
})

// /bestScores endpoint
app.get('/bestScores/:limit?', async (req, res) => {
    let limit = req.params.limit ? req.params.limit : 50
    const scores = await docStore.getBestScores(limit)
    let msg = {count: scores.length, scores: scores}
    res.send(msg)
})

app.get('/getRoundsUnderPar', async (req, res) => {
    const scores = await docStore.getRoundsUnderPar()
    let msg = {count: scores.length, scores: scores}
    res.send(msg)
})

app.get('/getByScore/:score?', async (req, res) => {
    let score = req.params.score ? req.params.score : 36
    const scores = await docStore.getByScore(parseInt(score))
    let msg = {count: scores.length, scores: scores}
    res.send(msg)
})

app.get('/getByGolfer/:lastName?', async (req, res) => {
    let str = req.params.lastName || ''
    const scores = await docStore.getByGolfer(str)
    let msg = {count: scores.length, scores: scores}
    res.send(msg)
})

app.get('/getAverageScorePerGolfer/:year?', async (req, res) => {
    const scores = await docStore.getAverageScorePerGolfer(req.params.year)
    let msg = {count: scores.length, scores: scores}
    res.send(msg)

})

app.get('/getCourseScoringData', async (req, res) => {
    const scores = await docStore.getCourseScoringData()
    let msg = {count: scores.length, scores: scores}
    res.send(msg)
})

app.get('/getHolesInOne', async (req, res) => {
    const aces = await docStore.getHolesInOne()
    let msg = {count: aces.length, aces: aces}
    res.send(msg)
})

app.get('/getAggregateCourseScore', async (req, res) => {
    const courses = await docStore.getAggregateCourseScore()
    let msg = {courses: courses}
    res.send(msg)
})

app.post('/score', async function (req, res, response) {
    const success = await docStore.addScore(req.body);
    let msg = {success: success}
    res.send(msg)
})

app.post('/holeScores', async function (req, res, response) {
    const success = await docStore.addHoleScores(req.body);
    let msg = {success: success}
    res.send(msg)
})

app.get('/removeScore/:id?', async function (req, res, response) {
    let msg = {}
    if (req.params.id) {
        let success = await docStore.removeScore(req.params.id)
        msg.success = success
    }
    else {
        msg.error = "Please provide a valid id."
    }
    res.send(msg)
})