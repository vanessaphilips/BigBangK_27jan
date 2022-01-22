/**
 * global declaration of header used in fetch where
 * a token is needed
 *
 * @param token bearer jwt
 * @returns {*[]}  a 2d array of key value pairs containing headers
 */
function acceptHeadersWithToken(token) {
    const accept = []
    accept.push(['Accept', 'Application/json'])
    accept.push(["content-type", "application/json"])
    accept.push(['Access-Control-Allow-Origin', '*'])
    accept.push(['Access-Control-Allow-Methods', '*'])
    accept.push(['authorization', token])
    return accept
}
/**
 * global declaration of header used in fetch where
 * a no token is needed
 *
 * @returns {*[]}a 2d array of key value pairs containing headers
 */
function acceptHeaders() {
    const accept = []
    accept.push(['Accept', 'Application/json'])
    accept.push(["content-type", "application/json"])
    accept.push(['Access-Control-Allow-Origin', '*'])
    accept.push(['Access-Control-Allow-Methods', '*'])
    return accept
}