package hub

class RegisterController {
    def mailService
    
    def index() {
        [company: new Company(params)]
    }
    
    def save() {
        def company = new Company(params)
        
        def samePasswords = (params['password'] == params['repassword'])
        
        if(!samePasswords) {
            company.errors.rejectValue('password', 'password.doesnotmatch', 'Passwords don\'t match')
            render(model: [company: company], view: 'index')
            return
        }
        if(!company.save(flush:true)){
            render(model: [company: company], view: 'index')
            return 
        }
        
        mailService.sendMail {     
          to company.email
          subject "[CachiruloHub] " + message(code: "hub.register.subject", default: "Confirm your email")
          html g.render(template:"/mails/confirmation", model:[company: company])
        }
        flash.message = message(code: "hub.register.sent", default: "We have sent an email to") + " ${company.email} " + message(code: "hub.register.confirm", default: "to confirm the registration")
        session.company = company
        redirect(controller:'company', action:'edit', id:company.id)
    }
    
    def confirm() {
        def company = Company.findByKey(params.id)
        if(company){
            company.enabled = true
            company.save(flush:true)
            flash.message = message(code: "hub.register.confirmed", default: "Your account has been enabled")
            redirect(controller:'company', action:'show', id: company.id)
        }
    }
}
