console.log("this is script file")

const toggleSidebar=()=>{

    if($(".sidebar").is(":visible")){

        $(".sidebar").css("display", "none");
        $(".content").css("margin-left","0%");

    }else{

        $(".sidebar").css("display", "block");
        $(".content").css("margin-left","20%");

    }

};

const search = () =>{
    let query = $("#search-input").val();

    if(query==""){
        $(".search-result").hide();

    }else{
        //sending request to server

        let url =`http://localhost:8080/search/${query}`;

        fetch(url).then((response) => {
            return response.json();
        })
        .then((data) => {
            let text = `<div class='list-group'>`

            data.forEach(contact => {
                text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
            });

            text += `</div>`;

            $(".search-result").html(text);
            $(".search-result").show();

        });

        $(".search-result").show();
    }

};


//first request to server to create order

const paymentStart=()=>{
    console.log("payment start");
    let amount =$("#payment_field").val();
    if(amount=="" || amount==null){
        //alert("amount is required !!");
  
        Swal.fire(
            'OOPS!',
            'Amount is required!',
            'error'
          )
        return;
    }

    //code..
    //ajax code to send request to server to create order

    $.ajax(
        {
            url:'/user/create_order',
            data:JSON.stringify({amount:amount,info:'order_request'}),
            contentType:'application/json',
            type:'POST',
            dataType:'json',
            success:function(response){
                //invoked when success
                console.log(response)
                if(response.status == "created"){
                    //open payment form
                    let options={
                        key:'rzp_test_BAILOBGbIhmAlb',
                        amount:response.amount,
                        currency:'INR',
                        name:'Smart contact manager',
                        description:'Donation',
                        Image:'https://img.freepik.com/free-vector/hands-connecting-logo_23-2147507857.jpg?w=740&t=st=1696357393~exp=1696357993~hmac=312f3875b6371bd47c6ee0005cdc118c097e1b424efa6bf11594b9924524f2c7',
                        order_id:response.id,
                        handler:function(response){
                            console.log(response.razorpay_payment_id)
                            console.log(response.razorpay_order_id)
                            console.log('payment successful');

                            updatePaymentOnServer(response.razorpay_payment_id,
                                        response.razorpay_order_id, "paid");

                            
                        },
                        prefill: {
                            "name": "",
                            "email": "",
                            "contact": "",
                        },
                        notes: {
                            address: "Bengaluru",
                        },
                        theme: {
                            color: "#3399cc"
                        },
                    };

                    let rzp = new Razorpay(options);

                    rzp.on('payment.failed', function (response){
                        console.log(response.error.code);
                        
                        console.log(response.error.source);
                        console.log(response.error.step);
                        console.log(response.error.reason);
                        console.log(response.error.metadata.order_id);
                        console.log(response.error.metadata.payment_id);
                        //alert("oops!! payment failed");
                        Swal.fire(
                            'OOPS!',
                            'Payment FAiled!',
                            'error'
                          )
                });
                    rzp.open();

                }
            },
            error:function(error){
                //invoked when error
                console.log(error)
                alert("something went wrong!")
            }
        }
    )

};

function updatePaymentOnServer(payment_id, order_id,status)
{
    $.ajax(
        {
            url:'/user/update_order',
            data:JSON.stringify({payment_id:payment_id,order_id:order_id, status: status,}),
            contentType:'application/json',
            type:'POST',
            dataType:'json',
            success:function(response){
                Swal.fire(
                    'Congrats!',
                    'Payment Successful!',
                    'success'
                  )
            },
            error:function(error){
                Swal.fire(
                    'Failed!',
                    'payment done, but its not reflected on server!',
                    'error'
                  )
            },
        });
}